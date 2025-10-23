package com.example.IMS_Backend.config;


public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
    public ProductNotFoundException() {
        super("Product not found");
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}