package com.example.IMS_Backend.repository;

import com.example.IMS_Backend.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
    List<StoredFile> findByOwnerTypeIgnoreCaseAndOwnerIdOrderByUploadedAtDesc(String ownerType, Long ownerId);
    List<StoredFile> findAllByOrderByUploadedAtDesc();
}
