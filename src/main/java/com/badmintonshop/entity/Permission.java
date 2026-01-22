package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.PermissionName;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents a specific granular authority within the Role-Based Access Control (RBAC) system.
 * <p>
 * Permissions are assigned to Roles, which are then assigned to Employees.
 * Examples: "PRODUCT_CREATE", "ORDER_VIEW".
 * </p>
 */
@Entity
@Table(name = "permissions")
@Data
public class Permission {

    /**
     * The unique identifier for the permission.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique code/name of the permission used in security checks.
     * <p>
     * Convention: UPPER_CASE with underscores (e.g., "USER_READ", "STOCK_UPDATE").
     * Must be unique across the system.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private PermissionName name;

    /**
     * A human-readable description of what this permission allows.
     * <p>
     * Displayed in the Admin Dashboard to help managers understand the permission's purpose.
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String description;
}