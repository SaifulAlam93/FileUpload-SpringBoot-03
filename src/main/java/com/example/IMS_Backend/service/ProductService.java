//package com.example.IMS_Backend.service;
//
//import com.example.IMS_Backend.config.FileStorageException;
//import com.example.IMS_Backend.config.ProductNotFoundException;
//import com.example.IMS_Backend.model.Product;
//import com.example.IMS_Backend.repository.ProductRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//public class ProductService {
//
//    private final ProductRepository productRepository;
//    private final Path uploadPath;
//
//    public ProductService(
//            ProductRepository productRepository,
//            @Value("${app.file.upload-dir:uploads}") String uploadDirectory
//    ) {
//        this.productRepository = productRepository;
//        this.uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
//
//        createUploadDirectory();
//    }
//
//    // ============================================================
//    // GET
//    // ============================================================
//
//    public List<Product> getAll() {
//        return productRepository.findAll();
//    }
//
//    public Product getById(Long id) {
//
//        return productRepository.findById(id)
//                .orElseThrow(() ->
//                        new ProductNotFoundException(
//                                "Product not found with id: " + id
//                        )
//                );
//    }
//
//    // ============================================================
//    // CREATE
//    // ============================================================
//
//    public Product create(
//            Product product,
//            MultipartFile[] imageFiles
//    ) {
//
//        // Image upload is OPTIONAL
//        List<String> imageUrls = saveImages(imageFiles);
//
//        if (!imageUrls.isEmpty()) {
//
//            product.setImageUrls(imageUrls);
//
//            // First image = main image
//            product.setImageUrl(imageUrls.get(0));
//
//        } else {
//
//            // No image uploaded
//            product.setImageUrls(new ArrayList<>());
//            product.setImageUrl(null);
//        }
//
//        return productRepository.save(product);
//    }
//
//    // ============================================================
//    // UPDATE
//    // ============================================================
//
//    public Product updateProduct(
//            Long id,
//            Product productDetails,
//            MultipartFile[] imageFiles
//    ) {
//
//        Product product = getById(id);
//
//        // --------------------------------------------------------
//        // Update basic product information
//        // --------------------------------------------------------
//
//        product.setName(productDetails.getName());
//        product.setDescription(productDetails.getDescription());
//        product.setPrice(productDetails.getPrice());
//        product.setQuantity(productDetails.getQuantity());
//        product.setCategory(productDetails.getCategory());
//
//        // --------------------------------------------------------
//        // Existing images
//        // --------------------------------------------------------
//
//        List<String> existingImages =
//                product.getImageUrls() != null
//                        ? new ArrayList<>(product.getImageUrls())
//                        : new ArrayList<>();
//
//        // --------------------------------------------------------
//        // New uploaded images
//        // --------------------------------------------------------
//
//        List<String> newImages = saveImages(imageFiles);
//
//        // --------------------------------------------------------
//        // IMPORTANT:
//        // If no new files are uploaded, keep existing images.
//        // If new files are uploaded, add them.
//        // --------------------------------------------------------
//
//        if (!newImages.isEmpty()) {
//            existingImages.addAll(newImages);
//        }
//
//        // --------------------------------------------------------
//        // Update images
//        // --------------------------------------------------------
//
//        product.setImageUrls(existingImages);
//
//        if (!existingImages.isEmpty()) {
//            product.setImageUrl(existingImages.get(0));
//        } else {
//            product.setImageUrl(null);
//        }
//
//        return productRepository.save(product);
//    }
//
//    // ============================================================
//    // DELETE
//    // ============================================================
//
//    public void deleteProduct(Long id) {
//
//        Product product = getById(id);
//
//        // Delete associated image files
//        deleteImages(product.getImageUrls());
//
//        productRepository.delete(product);
//    }
//
//    // ============================================================
//    // FILE OPERATIONS
//    // ============================================================
//
//    /**
//     * Saves uploaded images.
//     *
//     * File upload is optional.
//     *
//     * null array        -> no images
//     * empty array       -> no images
//     * empty file        -> ignored
//     * valid files       -> saved
//     */
//    private List<String> saveImages(
//            MultipartFile[] imageFiles
//    ) {
//
//        if (imageFiles == null || imageFiles.length == 0) {
//            return Collections.emptyList();
//        }
//
//        List<String> imageUrls = new ArrayList<>();
//
//        for (MultipartFile file : imageFiles) {
//
//            if (file == null || file.isEmpty()) {
//                continue;
//            }
//
//            imageUrls.add(saveImage(file));
//        }
//
//        return imageUrls;
//    }
//
//    // ============================================================
//    // SAVE SINGLE IMAGE
//    // ============================================================
//
//    private String saveImage(MultipartFile file) {
//
//        try {
//
//            createUploadDirectory();
//
//            String originalFilename =
//                    file.getOriginalFilename();
//
//            if (originalFilename == null
//                    || originalFilename.isBlank()) {
//
//                throw new FileStorageException(
//                        "Invalid file name"
//                );
//            }
//
//            // Remove possible path information
//            String cleanFilename = Paths
//                    .get(originalFilename)
//                    .getFileName()
//                    .toString();
//
//            // Generate unique filename
//            String fileName =
//                    UUID.randomUUID()
//                            + "_"
//                            + cleanFilename;
//
//            Path targetLocation =
//                    uploadPath
//                            .resolve(fileName)
//                            .normalize();
//
//            // Prevent path traversal
//            if (!targetLocation.startsWith(uploadPath)) {
//
//                throw new FileStorageException(
//                        "Invalid file path"
//                );
//            }
//
//            Files.copy(
//                    file.getInputStream(),
//                    targetLocation,
//                    StandardCopyOption.REPLACE_EXISTING
//            );
//
//            // Store only the URL/path in database
//            return "/uploads/" + fileName;
//
//        } catch (IOException e) {
//
//            throw new FileStorageException(
//                    "Could not store file: "
//                            + file.getOriginalFilename(),
//                    e
//            );
//        }
//    }
//
//    // ============================================================
//    // DELETE IMAGES
//    // ============================================================
//
//    private void deleteImages(
//            List<String> imageUrls
//    ) {
//
//        if (imageUrls == null || imageUrls.isEmpty()) {
//            return;
//        }
//
//        for (String imageUrl : imageUrls) {
//
//            if (imageUrl == null || imageUrl.isBlank()) {
//                continue;
//            }
//
//            try {
//
//                String fileName = Paths
//                        .get(imageUrl)
//                        .getFileName()
//                        .toString();
//
//                Path filePath =
//                        uploadPath
//                                .resolve(fileName)
//                                .normalize();
//
//                // Security check
//                if (!filePath.startsWith(uploadPath)) {
//                    continue;
//                }
//
//                Files.deleteIfExists(filePath);
//
//            } catch (IOException e) {
//
//                // Do not fail product deletion
//                // because image deletion failed.
//                System.err.println(
//                        "Could not delete image: "
//                                + imageUrl
//                );
//            }
//        }
//    }
//
//    // ============================================================
//    // CREATE UPLOAD DIRECTORY
//    // ============================================================
//
//    private void createUploadDirectory() {
//
//        try {
//
//            if (!Files.exists(uploadPath)) {
//
//                Files.createDirectories(
//                        uploadPath
//                );
//            }
//
//        } catch (IOException e) {
//
//            throw new FileStorageException(
//                    "Could not create upload directory: "
//                            + uploadPath,
//                    e
//            );
//        }
//    }
//}

package com.example.IMS_Backend.service;

import com.example.IMS_Backend.config.FileStorageException;
import com.example.IMS_Backend.config.ProductNotFoundException;
import com.example.IMS_Backend.model.Product;
import com.example.IMS_Backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final Path uploadPath;

    public ProductService(ProductRepository productRepository,
                          @Value("${app.file.upload-dir:uploads}") String uploadDirectory) {

        this.productRepository = productRepository;

        this.uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    // =========================================================
    // GET ALL
    // =========================================================

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    public Product getById(Long id) {

        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    // =========================================================
    // CREATE
    // =========================================================

    public Product create(Product product, MultipartFile[] imageFiles) {
        List<String> imageUrls = new ArrayList<>();
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = saveImage(file);
                    imageUrls.add(imageUrl);
                }
            }
        }
        product.setImageUrls(imageUrls);
        return productRepository.save(product);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public Product updateProduct(Long id, Product productDetails, MultipartFile[] imageFiles) {

        Product product = productRepository.findById(id).orElseThrow(() ->
                new ProductNotFoundException("Product not found with id " + id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());
        product.setCategory(productDetails.getCategory());

        // -----------------------------------------------------
        // OLD IMAGES
        // -----------------------------------------------------

        List<String> oldImages = product.getImageUrls() != null ? product.getImageUrls() : new ArrayList<>();

        // -----------------------------------------------------
        // IMAGES KEPT BY ANGULAR
        // -----------------------------------------------------

        List<String> keptImages = productDetails.getImageUrls() != null ? productDetails.getImageUrls() : new ArrayList<>();

        // -----------------------------------------------------
        // FIND REMOVED IMAGES
        // -----------------------------------------------------

        Set<String> keptImageSet = new HashSet<>(keptImages);

        for (String oldImage : oldImages) {

            if (!keptImageSet.contains(oldImage)) {

                deleteImage(oldImage);
            }
        }

        // -----------------------------------------------------
        // FINAL IMAGE LIST
        // -----------------------------------------------------

        List<String> finalImages = new ArrayList<>(keptImages);

        // -----------------------------------------------------
        // ADD NEW UPLOADED IMAGES
        // -----------------------------------------------------

        if (imageFiles != null) {

            for (MultipartFile file : imageFiles) {

                if (file != null && !file.isEmpty()) {

                    String imageUrl = saveImage(file);

                    finalImages.add(imageUrl);
                }
            }
        }

        product.setImageUrls(finalImages);

        return productRepository.save(product);
    }

    // =========================================================
    // DELETE PRODUCT
    // =========================================================

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));

        // -----------------------------------------------------
        // DELETE ALL PRODUCT IMAGES FROM DISK
        // -----------------------------------------------------

        if (product.getImageUrls() != null) {

            for (String imageUrl : product.getImageUrls()) {

                deleteImage(imageUrl);
            }
        }

        // -----------------------------------------------------
        // DELETE PRODUCT FROM DATABASE
        // -----------------------------------------------------

        productRepository.delete(product);
    }

    // =========================================================
    // SAVE IMAGE
    // =========================================================

    private String saveImage(MultipartFile imageFile) {

        try {

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = imageFile.getOriginalFilename();
            //abc.lmn.xyz.png

            // Get extension
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            } //.png

            // Generate unique filename
            String fileName = UUID.randomUUID() + extension;
            //9a04efca-b1eb-47ce-9c66-63434c4a768a.png

            Path filePath = uploadPath.resolve(fileName).normalize();

            // Save file
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL
//            return ServletUriComponentsBuilder.fromCurrentContextPath().path("/uploads/").path(fileName).toUriString();
            return "/uploads/"+fileName;


        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + imageFile.getOriginalFilename(), e);
        }
    }

    // =========================================================
    // DELETE IMAGE FROM DISK
    // =========================================================

    private void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = uploadPath.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not delete image: " + imageUrl, e);
        }
    }
}