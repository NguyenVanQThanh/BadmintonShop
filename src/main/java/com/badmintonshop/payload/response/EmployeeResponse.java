package com.badmintonshop.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for returning Employee details.
 * <p>
 * Safe for client-side usage (No passwords, flattened structure).
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;

    private String email;

    private String fullName;

    private String employeeCode;

    private String phoneNumber;

    /**
     * Role name as String (e.g., "ADMIN", "STAFF") for easy display on Frontend.
     */
    private String role;

    /**
     * Matches the 'enabled' field in Entity.
     * Used to show if the account is active or banned.
     */
    private boolean enabled;
}