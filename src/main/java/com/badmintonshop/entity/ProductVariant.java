package com.badmintonshop.entity;

import com.badmintonshop.entity.json.ProductAttributes;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific Stock Keeping Unit (SKU) or variation of a product.
 * <p>
 * While the {@link Product} entity holds general information (Name, Brand),
 * this entity holds the specific sellable inventory data (Price, Stock, Specific Specs).
 * Example: "Yonex Astrox 100ZZ" is the Product, but "4U-G5 version" is the Variant.
 * </p>
 */
@Entity
@Table(name = "product_variants")
@Data
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent product to which this variant belongs.
     * <p>
     * <b>FetchType.LAZY:</b> Used for performance optimization. The parent product data 
     * is only loaded from the database when explicitly accessed via {@code getProduct()}.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude // Critical: Prevents circular reference loops during logging
    private Product product;

    /**
     * Unique Stock Keeping Unit (SKU) code.
     * <p>
     * Used for inventory tracking and barcode scanning.
     * Example: "100ZZ-4U-G5"
     * </p>
     */
    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private Double price;

    private Integer stockQuantity = 0;

    /**
     * Polymorphic column storing category-specific technical attributes.
     * <p>
     * Mapped to a PostgreSQL <b>JSONB</b> column for schema flexibility.
     * This field can hold {@link com.badmintonshop.entity.json.RacketAttributes}, 
     * {@link com.badmintonshop.entity.json.ShoeAttributes}, etc., based on the context.
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private ProductAttributes attributes;

    /**
     * A list of specific image URLs for this variant.
     * <p>
     * Stores multiple images (e.g., different angles of a specific colorway) 
     * as a JSON array in the database.
     * Example: {@code ["url_front.jpg", "url_side.jpg", "url_sole.jpg"]}
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> imageUrls = new ArrayList<>();
}