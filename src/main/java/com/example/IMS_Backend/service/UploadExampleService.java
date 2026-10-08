package com.example.IMS_Backend.service;

import com.example.IMS_Backend.model.StoredFile;
import com.example.IMS_Backend.model.UploadExample;
import com.example.IMS_Backend.repository.UploadExampleRepository;
import com.example.IMS_Backend.dto.UploadExampleImageDto;
import com.example.IMS_Backend.dto.UploadExampleRequestDto;
import com.example.IMS_Backend.dto.UploadExampleResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class UploadExampleService {
    private static final String OWNER_TYPE = "UploadExample";

    private final UploadExampleRepository repository;
    private final StoredFileService storedFileService;

    public UploadExampleService(UploadExampleRepository repository, StoredFileService storedFileService) {
        this.repository = repository;
        this.storedFileService = storedFileService;
    }

    @Transactional(rollbackFor = IOException.class)
    public UploadExampleResponseDto create(UploadExampleRequestDto request, MultipartFile[] images) throws IOException {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("Select at least one image.");
        }
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty() || image.getContentType() == null
                    || !image.getContentType().toLowerCase().startsWith("image/")) {
                throw new IllegalArgumentException("Every selected file must be a non-empty image.");
            }
        }

        UploadExample example = new UploadExample(request.getTitle().trim(), request.getDescription().trim(),
                request.getReferenceCode().trim(), request.getQuantity(), request.getUnitPrice());
        UploadExample saved = repository.saveAndFlush(example);
        List<StoredFile> files = storedFileService.uploadMany(images, OWNER_TYPE, saved.getId());
        return toResponse(saved, files);
    }

    @Transactional(readOnly = true)
    public List<UploadExampleResponseDto> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(example -> toResponse(example,
                        storedFileService.listForOwner(OWNER_TYPE, example.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UploadExampleResponseDto get(long id) {
        UploadExample example = repository.findById(id)
                .orElseThrow(() -> new UploadExampleNotFoundException(id));
        return toResponse(example, storedFileService.listForOwner(OWNER_TYPE, id));
    }

    private UploadExampleResponseDto toResponse(UploadExample example, List<StoredFile> files) {
        List<UploadExampleImageDto> images = files.stream().map(file -> new UploadExampleImageDto(file.getId(),
                file.getOriginalFilename(), file.getContentType(), file.getSize(),
                "/api/files/" + file.getId() + "/view", "/api/files/" + file.getId() + "/download"))
                .toList();
        return new UploadExampleResponseDto(example.getId(), example.getTitle(), example.getDescription(),
                example.getReferenceCode(), example.getQuantity(), example.getUnitPrice(),
                example.getCreatedAt(), images);
    }

    public static class UploadExampleNotFoundException extends RuntimeException {
        public UploadExampleNotFoundException(long id) {
            super("Upload example " + id + " was not found.");
        }
    }
}
