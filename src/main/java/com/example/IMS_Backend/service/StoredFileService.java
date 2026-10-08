package com.example.IMS_Backend.service;

import com.example.IMS_Backend.model.StoredFile;
import com.example.IMS_Backend.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class StoredFileService {
    private final StoredFileRepository repository;
    private final Path storageDirectory;

    public StoredFileService(StoredFileRepository repository,
                             @Value("${app.file-storage.directory:uploads/library}") String directory) {
        this.repository = repository;
        this.storageDirectory = Path.of(directory).toAbsolutePath().normalize();
    }

    @Transactional
    public StoredFile upload(MultipartFile file) throws IOException {
        return upload(file, null, null);
    }

    @Transactional
    public StoredFile upload(MultipartFile file, String ownerType, Long ownerId) throws IOException {
        return store(file, ownerType, ownerId);
    }

    @Transactional(rollbackFor = IOException.class)
    public List<StoredFile> uploadMany(MultipartFile[] files, String ownerType, Long ownerId) throws IOException {
        List<String> storedKeys = new ArrayList<>();
        List<StoredFile> storedFiles = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                StoredFile stored = store(file, ownerType, ownerId);
                storedKeys.add(stored.getStorageKey());
                storedFiles.add(stored);
            }
            return storedFiles;
        } catch (IOException | RuntimeException exception) {
            for (String key : storedKeys) Files.deleteIfExists(resolve(key));
            throw exception;
        }
    }

    private StoredFile store(MultipartFile file, String ownerType, Long ownerId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a non-empty file to upload.");
        }
        String normalizedOwnerType = normalizeOwnerType(ownerType, ownerId);

        Files.createDirectories(storageDirectory);
        String storageKey = UUID.randomUUID().toString();
        Path destination = resolve(storageKey);
        String originalName = cleanFilename(file.getOriginalFilename());
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        contentType = contentType.substring(0, Math.min(contentType.length(), 150));

        try {
            try (var input = file.getInputStream()) {
                Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return repository.save(new StoredFile(originalName, storageKey, contentType, file.getSize(),
                    normalizedOwnerType, ownerId));
        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(destination);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<StoredFile> list() {
        return repository.findAllByOrderByUploadedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<StoredFile> listForOwner(String ownerType, long ownerId) {
        String normalizedOwnerType = normalizeOwnerType(ownerType, ownerId);
        return repository.findByOwnerTypeIgnoreCaseAndOwnerIdOrderByUploadedAtDesc(normalizedOwnerType, ownerId);
    }

    @Transactional
    public StoredFile update(long id, String originalFilename, String ownerType, Long ownerId) {
        StoredFile file = get(id);
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be empty.");
        }
        file.setOriginalFilename(cleanFilename(originalFilename));
        file.setOwnerType(normalizeOwnerType(ownerType, ownerId));
        file.setOwnerId(ownerId);
        return repository.save(file);
    }

    @Transactional(readOnly = true)
    public StoredFile get(long id) {
        return repository.findById(id).orElseThrow(() -> new StoredFileNotFoundException(id));
    }

    public Resource load(StoredFile file) throws IOException {
        Resource resource = new UrlResource(resolve(file.getStorageKey()).toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new StoredFileNotFoundException(file.getId());
        }
        return resource;
    }

    @Transactional
    public void delete(long id) throws IOException {
        StoredFile file = get(id);
        Files.deleteIfExists(resolve(file.getStorageKey()));
        repository.delete(file);
    }

    private Path resolve(String storageKey) {
        Path resolved = storageDirectory.resolve(storageKey).normalize();
        if (!resolved.getParent().equals(storageDirectory)) {
            throw new IllegalArgumentException("Invalid file storage key.");
        }
        return resolved;
    }

    private String cleanFilename(String filename) {
        if (filename == null || filename.isBlank()) return "download";
        String leafName = Path.of(filename.replace('\\', '/')).getFileName().toString();
        String cleaned = leafName.replaceAll("[\\p{Cntrl}]", "_").trim();
        if (cleaned.isBlank() || cleaned.equals(".") || cleaned.equals("..")) return "download";
        return cleaned.substring(0, Math.min(cleaned.length(), 255));
    }

    private String normalizeOwnerType(String ownerType, Long ownerId) {
        if ((ownerType == null || ownerType.isBlank()) && ownerId == null) return null;
        if (ownerType == null || ownerType.isBlank() || ownerId == null || ownerId < 1) {
            throw new IllegalArgumentException("Provide both entityType and a positive entityId, or neither.");
        }
        String normalized = ownerType.trim();
        if (!normalized.matches("[A-Za-z][A-Za-z0-9_-]{0,79}")) {
            throw new IllegalArgumentException("entityType must start with a letter and contain only letters, numbers, _ or -.");
        }
        return normalized;
    }

    public static class StoredFileNotFoundException extends RuntimeException {
        public StoredFileNotFoundException(long id) {
            super("File " + id + " was not found.");
        }
    }
}
