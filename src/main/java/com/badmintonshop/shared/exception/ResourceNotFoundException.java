package com.badmintonshop.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a requested resource (e.g., User, Product, Order)
 * cannot be found in the system.
 * <p>
 * This exception is typically handled by the GlobalExceptionHandler to return
 * an HTTP 404 (Not Found) status code to the client.
 * </p>
 * * Usage Example:
 * throw new ResourceNotFoundException("Product not found with id: " + id);
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Fallback: If GlobalHandler misses it, Spring returns 404 automatically.
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Unique identifier for Serializable class versioning.
     * Prevents InvalidClassException during deserialization if the class changes.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message The detail message explaining which specific resource was not found.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}