package com.example.IMS_Backend.repository;

import com.example.IMS_Backend.model.UploadExample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadExampleRepository extends JpaRepository<UploadExample, Long> {
    List<UploadExample> findAllByOrderByCreatedAtDesc();
}
