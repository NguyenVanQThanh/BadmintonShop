package com.badmintonshop.repository;

import com.badmintonshop.entity.Permission;
import com.badmintonshop.entity.enums.PermissionName;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * Finds a permission by its name.
     * Used by DataSeeder to check if a permission already exists before creating it.
     *
     * @param name The name of the permission (e.g., "PRODUCT_READ")
     * @return Optional containing the permission if found, or empty otherwise.
     */
    Optional<Permission> findByName(PermissionName name);
}