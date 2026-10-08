package com.example.IMS_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadExampleImageDto {
    private Long id;
    private String originalFilename;
    private String contentType;
    private long size;
    private String viewUrl;
    private String downloadUrl;
}
