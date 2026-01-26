package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the base definition of a product in the catalog.
 * <p>
 * This entity acts as the "Parent" record containing shared attributes 
 * (Name, Brand, Description) that apply to all its specific variations (SKUs).
 * </p>
 */
@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The commercial name of the product.
     * Example: "Yonex Astrox 100ZZ"
     */
    @Column(nullable = false)
    private String name;

    /**
     * The manufacturer or brand name.
     * Examples: "Yonex", "Lining", "Victor"
     */
    private String brand;

    /**
     * A detailed description or marketing copy of the product.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The primary image URL used for thumbnails in product listings or search results.
     * <p>
     * Note: Detailed, specific images for each color/variation are stored 
     * within the {@link ProductVariant} entity.
     * </p>
     */
    private String thumbnailUrl;

    /**
     * Controls the visibility of the product on the storefront.
     * Set to false to hide the product without deleting the record.
     */
    private boolean isActive = true;

    /**
     * One-to-Many relationship: A product has multiple specific variants (SKUs).
     * <p>
     * <b>CascadeType.ALL:</b> Persisting or removing a Product will automatically 
     * propagate the operation to its variants.
     * <br>
     * <b>orphanRemoval = true:</b> Removing a variant from this list will 
     * automatically delete the corresponding record from the database.
     * </p>
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Critical: Prevents circular reference loops (StackOverflowError) in Lombok's toString()
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}