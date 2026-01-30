package com.badmintonshop.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) for user authentication requests.
 * <p>
 * This class captures the credentials sent by the client during the login process.
 * It uses Jakarta Validation constraints to ensure data integrity before processing.
 * </p>
 */
@Data
public class LoginRequest {

    /**
     * The user's email address, serving as the unique username.
     * <p>
     * Constraints:
     * 1. Must not be null or empty.
     * 2. Should follow a valid email format (optional but recommended).
     * </p>
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid") // Optional: Adds format validation
    private String email;

    /**
     * The user's raw password.
     * <p>
     * <strong>SECURITY NOTE:</strong> This field is excluded from the generated toString() method
     * to prevent accidental leakage of sensitive credentials in application logs.
     * </p>
     */
    @NotBlank(message = "Password is required")
    @ToString.Exclude // CRITICAL: Never print passwords in logs!
    private String password;
}