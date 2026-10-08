package com.example.IMS_Backend.controller;

import com.example.IMS_Backend.service.UploadExampleService;
import com.example.IMS_Backend.service.UploadExampleService.UploadExampleNotFoundException;
import com.example.IMS_Backend.dto.UploadExampleRequestDto;
import com.example.IMS_Backend.dto.UploadExampleResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload-examples")
public class UploadExampleController {
    private final UploadExampleService service;

    public UploadExampleController(UploadExampleService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadExampleResponseDto> create(
            @Valid @RequestPart("data") UploadExampleRequestDto entityData,
            @RequestPart("images") MultipartFile[] images) throws IOException {
        return ResponseEntity.status(201).body(service.create(entityData, images));
    }

    @GetMapping
    public List<UploadExampleResponseDto> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public UploadExampleResponseDto get(@PathVariable long id) {
        return service.get(id);
    }

    @ExceptionHandler(UploadExampleNotFoundException.class)
    public ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
