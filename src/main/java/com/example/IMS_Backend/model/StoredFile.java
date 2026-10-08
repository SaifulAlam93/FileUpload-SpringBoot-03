package com.example.IMS_Backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "stored_files")
@Getter
@Setter
@NoArgsConstructor
public class StoredFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, unique = true, length = 36)
    private String storageKey;

    @Column(nullable = false, length = 150)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(length = 80)
    private String ownerType;

    private Long ownerId;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    public StoredFile(String originalFilename, String storageKey, String contentType, long size) {
        this(originalFilename, storageKey, contentType, size, null, null);
    }

    public StoredFile(String originalFilename, String storageKey, String contentType, long size,
                      String ownerType, Long ownerId) {
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.size = size;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.uploadedAt = Instant.now();
    }
}
