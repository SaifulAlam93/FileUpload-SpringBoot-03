package com.example.IMS_Backend.service;

import com.example.IMS_Backend.config.FileStorageException;
import com.example.IMS_Backend.config.ProductNotFoundException;
import com.example.IMS_Backend.model.Product;
import com.example.IMS_Backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final String UPLOAD_DIR = "uploads/";

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ------------------- GET -------------------
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // ------------------- CREATE -------------------

    // Single file
    public Product createProduct(Product product, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            product.setImageUrl(imageUrl);
            product.setImageUrls(List.of(imageUrl));
        }
        return productRepository.save(product);
    }

    // Multiple files
    public Product createProduct(Product product, MultipartFile[] imageFiles) {
        if (imageFiles != null && imageFiles.length > 0) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    imageUrls.add(saveImage(file));
                }
            }
            product.setImageUrls(imageUrls);
//            product.setImageUrl(imageUrls.get(0)); // set first as main image
        }
        return productRepository.save(product);
    }

    // ------------------- UPDATE -------------------

    public Product updateProduct(Long id, Product productDetails, MultipartFile[] imageFiles) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());
        product.setCategory(productDetails.getCategory());

        // start with existing images sent by Angular
        List<String> finalImages = new ArrayList<>(
                productDetails.getImageUrls() != null ? productDetails.getImageUrls() : List.of()
        );

        // add newly uploaded images
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    finalImages.add(saveImage(file));
                }
            }
        }

        product.setImageUrls(finalImages);

        return productRepository.save(product);
    }


    // ------------------- DELETE -------------------
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));
        productRepository.delete(product);
    }

    // ------------------- HELPER -------------------
    private String saveImage(MultipartFile imageFile) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL for frontend
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + imageFile.getOriginalFilename(), e);
        }
    }
}

