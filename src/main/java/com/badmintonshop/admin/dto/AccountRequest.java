package com.badmintonshop.admin.dto;

import com.badmintonshop.shared.entity.enums.RoleName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for Account creation and update requests.
 * <p>
 * This class carries data from the client to the server.
 * Automatic validation annotations are applied to ensure data integrity
 * before reaching the service layer.
 * </p>
 */
@Data
public class AccountRequest {

    /**
     * The employee's email address. Must be unique in the system.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * The password for the new account.
     * <p>
     * Note: This field is typically ignored or handled differently during updates.
     * </p>
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    /**
     * The specific role assigned to the employee.
     * <p>
     * Using the Enum type ensures strict type safety.
     * If the JSON value does not match any enum constant (e.g., "ADMIN", "STAFF"),
     * a HttpMessageNotReadableException will be thrown automatically.
     * </p>
     */
    @NotNull(message = "Role is required")
    private RoleName role;
}