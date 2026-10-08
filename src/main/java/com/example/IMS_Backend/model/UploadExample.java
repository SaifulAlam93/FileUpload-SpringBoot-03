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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "upload_examples")
@Getter
@Setter
@NoArgsConstructor
public class UploadExample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, unique = true, length = 60)
    private String referenceCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UploadExample(String title, String description, String referenceCode,
                         Integer quantity, BigDecimal unitPrice) {
        this.title = title;
        this.description = description;
        this.referenceCode = referenceCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
