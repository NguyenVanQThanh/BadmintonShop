package com.badmintonshop.entity;

import java.util.Set;

import com.badmintonshop.entity.enums.RoleName;

import jakarta.persistence.*;
import lombok.Data;
/**
 * Represents a Role within the Role-Based Access Control (RBAC) system.
 * <p>
 * Roles are collections of Permissions assigned to Employees.
 * Examples: "ADMIN", "MANAGER"
 * </p>
 */
@Entity
@Table(name = "roles")
@Data
public class Role {
    /**
     * The unique identifier for the role.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique name of the role.
     * <p>
     * Convention: UPPER_CASE with underscores (e.g., "ADMIN", "MANAGER")
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleName name;

    /**
     * A human-readable description of what this permission allows.
     * <p>
     * Displayed in the Admin Dashboard to help managers understand the permission's purpose.
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The set of permissions associated with this role.
     * <p>
     * Eagerly fetched to ensure permissions are available when roles are loaded.
     * </p>
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions"
        , joinColumns = @JoinColumn(name = "role_id")
        , inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}
