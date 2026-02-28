package com.badmintonshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a specific Stock Keeping Unit (SKU) or a concrete variation of a Product.
 * <p>
 * While the {@link Product} entity defines the general information (e.g., "Yonex Astrox 77"),
 * the ProductVariant defines the specific physical item (e.g., "Yonex Astrox 77 - 4U/G5 - Red").
 * </p>
 * <p>
 * <b>Technical Note:</b> Uses Hibernate 6+ Native JSON support to store dynamic attributes
 * in PostgreSQL JSONB columns. No external libraries required.
 * </p>
 */
@Entity
@Table(name = "product_variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    /**
     * Unique identifier for the variant.
     * Primary Key, auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stock Keeping Unit (SKU) code.
     * <p>
     * A unique alphanumeric code used for inventory management and barcode scanning.
     * Must be unique across the entire system.
     * </p>
     */
    @Column(nullable = false, unique = true)
    private String sku;

    /**
     * The selling price of this specific variant.
     * <p>
     * <b>Mandatory:</b> Uses {@code BigDecimal} for financial calculations to ensure precision
     * and avoid floating-point errors (e.g., IEEE 754 issues with double).
     * </p>
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * Current available quantity in the warehouse.
     * <p>
     * Uses {@code @Builder.Default} to initialize stock to 0, preventing NullPointerExceptions
     * during business logic processing if the field is omitted.
     * </p>
     */
    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;

    /**
     * Specific image URL for this variant.
     * <p>
     * Useful when different variants have distinct appearances (e.g., Blue vs. Red colorways).
     * If null, the frontend should fallback to the parent Product's thumbnail.
     * </p>
     */
    @Column(name = "image_url")
    private String imageUrl;

    // ========================================================================
    // DYNAMIC ATTRIBUTES (NATIVE JSON)
    // ========================================================================

    /**
     * Stores dynamic product attributes using PostgreSQL JSONB.
     * <p>
     * <b>Usage:</b>
     * <ul>
     * <li>Key: Attribute name (e.g., "weight", "grip_size", "tension").</li>
     * <li>Value: Attribute value (e.g., "4U", "G5", "28lbs").</li>
     * </ul>
     * </p>
     * <p>
     * <b>Technical:</b> Annotated with {@code @JdbcTypeCode(SqlTypes.JSON)} to leverage
     * Hibernate 6's native JSON mapping capabilities.
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    // ========================================================================
    // RELATIONSHIPS
    // ========================================================================

    /**
     * The parent generic product definition.
     * <p>
     * <b>Performance:</b> Uses {@code FetchType.LAZY} to prevent loading heavy Product details
     * (like descriptions) when only variant info (like price/SKU) is needed (e.g., in a Cart).
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;
}