package com.example.IMS_Backend.controller;

import com.example.IMS_Backend.model.Product;
import com.example.IMS_Backend.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(
            @PathVariable Long id
    ) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity<Product> create(
            @RequestPart("product") Product product,
            @RequestPart(value = "images",required = false )   MultipartFile[] imageFiles
    ) {
        Product createdProduct = productService.create(product, imageFiles);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdProduct);
    }


    // ============================================================
    // UPDATE PRODUCT
    // ============================================================

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Product> updateProduct(

            @PathVariable Long id,

            @RequestPart("product")
            Product productDetails,

            @RequestPart(
                    value = "images",
                    required = false
            )
            MultipartFile[] imageFiles
    ) {

        Product updatedProduct =
                productService.updateProduct(
                        id,
                        productDetails,
                        imageFiles
                );

        return ResponseEntity.ok(updatedProduct);
    }

    // ============================================================
    // DELETE PRODUCT
    // ============================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id
    ) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}