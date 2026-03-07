package com.badmintonshop.admin.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard Data Transfer Object (DTO) for all API error responses.
 * <p>
 * This class ensures a consistent error structure across the application,
 * making it easier for frontend clients to parse and display error messages.
 * </p>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude fields with null values from the JSON response to keep it clean.
public class ErrorResponse {

    /**
     * The timestamp when the error occurred.
     * Useful for logging and debugging purposes.
     */
    private LocalDateTime timestamp;

    /**
     * The HTTP status code (e.g., 400, 401, 404, 500).
     */
    private int status;

    /**
     * The short error reason or HTTP status phrase (e.g., "Bad Request", "Unauthorized").
     */
    private String error;

    /**
     * A descriptive, user-friendly message explaining the error details.
     * This message can be displayed directly to the end-user.
     */
    private String message;

    /**
     * The URI path of the request that caused the error (e.g., "/api/auth/login").
     */
    private String path;

    /**
     * A map of field-specific validation errors.
     * <p>
     * Key: The name of the field that failed validation (e.g., "email").
     * Value: The validation error message (e.g., "Email must not be empty").
     * This field is only populated when a MethodArgumentNotValidException occurs.
     * </p>
     */
    private Map<String, String> validationErrors;
}