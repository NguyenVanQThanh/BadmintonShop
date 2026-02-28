package com.badmintonshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badmintonshop.entity.Brand;

/**
 * Repository interface for managing {@link Brand} entities.
 * <p>
 * Provides standard CRUD operations and custom queries for Brand management.
 * </p>
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Checks if a brand with the specified name already exists.
     * <p>
     * <b>Use Case:</b> Used for validation to prevent duplicate brand names 
     * when creating or updating a brand.
     * </p>
     *
     * @param name the brand name to check.
     * @return {@code true} if the brand exists, {@code false} otherwise.
     */
    boolean existsByName(String name);

    /**
     * Retrieves a brand by its exact name.
     * <p>
     * <b>Use Case:</b> Essential for Data Seeding (Excel Import) to lookup 
     * existing brands by text instead of ID.
     * </p>
     *
     * @param name the exact name of the brand to search for.
     * @return an {@link Optional} containing the found Brand, or empty if not found.
     */
    Optional<Brand> findByName(String name);
}