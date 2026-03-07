package com.badmintonshop.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.badmintonshop.shared.entity.enums.ProductStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a general product definition in the catalog.
 * <p>
 * A "Product" acts as the parent entity for specific stock keeping units (SKUs),
 * which are represented by the {@link ProductVariant} entity.
 * Example: "Yonex Astrox 77" is the Product, while "4U/G5 version" is the Variant.
 * </p>
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * Unique identifier for the product.
     * Primary Key, auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The commercial name of the product.
     * <p>
     * Used for display on listing pages and search queries.
     * </p>
     */
    @Column(nullable = false)
    private String name;

    /**
     * A comprehensive description of the product.
     * <p>
     * Stored as TEXT in the database to support long content, potentially including
     * HTML or Markdown formatting for rich text display.
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * URL to the main representative image of the product.
     * Displayed in product grids and search results.
     */
    @Column
    private String thumbnailUrl;

    /**
     * Controls the visibility and availability status of the product.
     * <p>
     * Uses {@link ProductStatus} enum to represent different states:
     * - IN_STOCK: Product is available for purchase and visible
     * - OUT_OF_STOCK: Product is temporarily out of stock
     * - DISCONTINUED: Product is no longer available
     * - COMING_SOON: Product will be available in the future
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.COMING_SOON;

    /**
     * The timestamp when the product was first created.
     * Automatically managed by Hibernate via @CreationTimestamp.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp of the last update to the product info.
     * Automatically updated by Hibernate via @UpdateTimestamp.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * The category this product belongs to.
     * <p>
     * FetchType.LAZY is used to optimize performance. Loading a list of products
     * should not automatically trigger queries for their categories unless accessed.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private Category category;

    /**
     * The brand or manufacturer of the product.
     * FetchType.LAZY prevents N+1 query issues when listing products.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brandId")
    private Brand brand;

    /**
     * The specific variations (SKUs) of this product.
     * <p>
     * - CascadeType.ALL: Saving the Product also saves its Variants.
     * - orphanRemoval=true: Removing a Variant from this list deletes it from the database.
     * - @Builder.Default: Essential to initialize the list as empty instead of null when using Builder.
     * </p>
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();
}
