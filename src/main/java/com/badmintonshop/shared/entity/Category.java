package com.badmintonshop.shared.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product category within the e-commerce system.
 * <p>
 * This entity supports a hierarchical structure (Adjacency List Model), allowing
 * categories to have infinite levels of sub-categories (e.g., Equipment -> Rackets -> Yonex).
 * </p>
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    /**
     * Unique identifier for the category.
     * Primary Key, auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The display name of the category (e.g., "Badminton Rackets").
     * This field is mandatory.
     */
    @Column(nullable = false)
    private String name;

    /**
     * A URL-friendly unique identifier for the category.
     * <p>
     * Used for generating SEO-friendly URLs (e.g., "badminton-rackets").
     * Must be unique across the system to avoid routing conflicts.
     * </p>
     */
    @Column(nullable = false, unique = true)
    private String slug;

    /**
     * A detailed description of the category.
     * Stored as TEXT in the database to allow longer content.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The URL or path to the category's thumbnail image.
     * Used for UI display purposes.
     */
    @Column
    private String imageUrl;

    // ========================================================================
    // HIERARCHICAL RELATIONSHIPS (Self-Referencing)
    // ========================================================================

    /**
     * The parent category of this current category.
     * <p>
     * - If null, this category is a "Root" category.
     * - FetchType.LAZY is used to prevent loading the parent automatically for performance.
     * - @JsonIgnore prevents infinite recursion loops during JSON serialization (Child -> Parent -> Child...).
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId")
    @JsonIgnore
    private Category parent;

    /**
     * The list of sub-categories (children) belonging to this category.
     * <p>
     * - CascadeType.ALL ensures that if this category is deleted, all its sub-categories
     * are also managed (or removed) depending on business logic.
     * </p>
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();
    
    /**
     * The list of products assigned to this specific category.
     * <p>
     * - @JsonIgnore is crucial here to avoid heavy data loading and infinite recursion
     * when fetching a category (we don't always want all products).
     * </p>
     */
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Product> products;
}