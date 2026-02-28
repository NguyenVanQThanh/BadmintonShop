package com.badmintonshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badmintonshop.entity.Category; // <--- SỬA LẠI IMPORT NÀY NGAY

/**
 * Repository interface for managing {@link Category} entities.
 * <p>
 * Handles hierarchical data retrieval (Parent-Child relationships) 
 * and SEO-friendly lookups via Slugs.
 * </p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Retrieves a category by its URL-friendly slug.
     * <p>
     * <b>Use Case:</b> Public-facing APIs where users navigate via URL 
     * (e.g., {@code /categories/badminton-rackets} instead of {@code /categories/1}).
     * </p>
     *
     * @param slug the unique slug string (e.g., "yonex-rackets").
     * @return an {@link Optional} containing the Category if found.
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Checks if a slug already exists in the database.
     * <p>
     * <b>Use Case:</b> Validation constraint to ensure no two categories 
     * share the same URL path.
     * </p>
     *
     * @param slug the slug to validate.
     * @return {@code true} if the slug is taken, {@code false} otherwise.
     */
    boolean existsBySlug(String slug);

    /**
     * Retrieves all top-level (Root) categories.
     * <p>
     * <b>Use Case:</b> Rendering the Main Menu or Navigation Bar. 
     * Root categories have no parent (parent_id is NULL).
     * </p>
     *
     * @return a list of root categories.
     */
    List<Category> findByParentIsNull();

    /**
     * Retrieves immediate sub-categories of a specific parent.
     * <p>
     * <b>Use Case:</b> Rendering dropdown menus or "Shop by Category" sections 
     * when a user clicks on a parent category.
     * </p>
     *
     * @param parentId the ID of the parent category.
     * @return a list of direct child categories.
     */
    List<Category> findByParentId(Long parentId);
}