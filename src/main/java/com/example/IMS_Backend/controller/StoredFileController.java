package com.example.IMS_Backend.controller;

import com.example.IMS_Backend.model.StoredFile;
import com.example.IMS_Backend.service.StoredFileService;
import com.example.IMS_Backend.service.StoredFileService.StoredFileNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class StoredFileController {
    private final StoredFileService service;

    public StoredFileController(StoredFileService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "entityType", required = false) String entityType,
                                               @RequestParam(value = "entityId", required = false) Long entityId) throws IOException {
        StoredFile saved = service.upload(file, entityType, entityId);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/files/{id}").buildAndExpand(saved.getId()).toUri())
                .body(toResponse(saved));
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileResponse>> uploadMany(@RequestParam("files") MultipartFile[] files,
                                                         @RequestParam("entityType") String entityType,
                                                         @RequestParam("entityId") long entityId) throws IOException {
        if (files.length == 0) throw new IllegalArgumentException("Choose at least one file to upload.");
        List<FileResponse> uploaded = service.uploadMany(files, entityType, entityId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.status(201).body(uploaded);
    }

    @GetMapping
    public List<FileResponse> list() {
        return service.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/by-entity")
    public List<FileResponse> listForEntity(@RequestParam("entityType") String entityType,
                                            @RequestParam("entityId") long entityId) {
        return service.listForOwner(entityType, entityId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public FileResponse get(@PathVariable long id) {
        return toResponse(service.get(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable long id) throws IOException {
        return fileResponse(id, ContentDisposition.attachment());
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable long id) throws IOException {
        return fileResponse(id, ContentDisposition.inline());
    }

    @PutMapping("/{id}")
    public FileResponse update(@PathVariable long id, @RequestBody UpdateFileRequest request) {
        return toResponse(service.update(id, request.originalFilename(), request.entityType(), request.entityId()));
    }

    private ResponseEntity<Resource> fileResponse(long id, ContentDisposition.Builder disposition) throws IOException {
        StoredFile file = service.get(id);
        return ResponseEntity.ok()
                .contentType(parseContentType(file.getContentType()))
                .contentLength(file.getSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition.filename(file.getOriginalFilename()).build().toString())
                .body(service.load(file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) throws IOException {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(StoredFileNotFoundException.class)
    public ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    private MediaType parseContentType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private FileResponse toResponse(StoredFile file) {
        return new FileResponse(file.getId(), file.getOriginalFilename(), file.getContentType(), file.getSize(),
                file.getUploadedAt(), file.getOwnerType(), file.getOwnerId(),
                "/api/files/" + file.getId() + "/download", "/api/files/" + file.getId() + "/view");
    }

    public record FileResponse(Long id, String originalFilename, String contentType, long size,
                               java.time.Instant uploadedAt, String entityType, Long entityId,
                               String downloadUrl, String viewUrl) {
    }

    public record UpdateFileRequest(String originalFilename, String entityType, Long entityId) {
    }
}
