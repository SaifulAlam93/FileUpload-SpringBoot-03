package com.example.IMS_Backend.controller;


import com.example.IMS_Backend.config.ProductNotFoundException;
import com.example.IMS_Backend.model.Product;
import com.example.IMS_Backend.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200") // your Angular frontend
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ------------------- GET ALL -------------------
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // ------------------- GET BY ID -------------------
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------- CREATE -------------------

    // Single file upload
    @PostMapping(value = "/single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProductSingle(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            Product createdProduct = productService.createProduct(product, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Multiple files upload
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProductMultiple(
            @RequestPart("product") Product product,
            @RequestPart(value = "images", required = false) MultipartFile[] imageFiles) {

        Product savedProduct = productService.createProduct(product, imageFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // ------------------- UPDATE -------------------

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") Product productDetails,
            @RequestPart(value = "images", required = false) MultipartFile[] imageFiles) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails, imageFiles);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------- DELETE -------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}




//import com.example.IMS_Backend.model.Product;
//import com.example.IMS_Backend.service.ProductService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/products")
//@CrossOrigin(origins = "http://localhost:4200")
//public class ProductController {
//
//    @Autowired
//    private ProductService productService;
//
//    @GetMapping
//    public List<Product> getAllProducts() {
//        return productService.getAllProducts();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
//        return productService.getProductById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<Product> createProduct(
//            @RequestPart("product") Product product,
//            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
//        try {
//            Product createdProduct = productService.createProduct(product, imageFile);
//            return ResponseEntity.ok(createdProduct);
//        } catch (IOException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Product> createProduct(
//            @RequestPart("product") Product product,
//            @RequestPart("images") MultipartFile[] imageFiles) throws IOException {
//
//        Product savedProduct = productService.createProduct(product, imageFiles);
//        return ResponseEntity.ok(savedProduct);
//    }
//
//
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Product> updateProduct(
//            @PathVariable Long id,
//            @RequestPart("product") Product productDetails,
//            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
//        try {
//            Product updatedProduct = productService.updateProduct(id, productDetails, imageFile);
//            return ResponseEntity.ok(updatedProduct);
//        } catch (Exception e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
//        try {
//            productService.deleteProduct(id);
//            return ResponseEntity.ok().build();
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//}