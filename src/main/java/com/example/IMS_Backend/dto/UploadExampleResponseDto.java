package com.example.IMS_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class UploadExampleResponseDto {
    private Long id;
    private String title;
    private String description;
    private String referenceCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Instant createdAt;
    private List<UploadExampleImageDto> images;
}
