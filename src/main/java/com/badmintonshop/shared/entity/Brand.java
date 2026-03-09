package com.badmintonshop.shared.entity;

import com.badmintonshop.shared.entity.enums.CommonStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a manufacturer or brand within the e-commerce system.
 * <p>
 * Brands (e.g., Yonex, Lining, Victor) are top-level entities used to categorize products.
 * Users typically filter products by Brand on the storefront.
 * </p>
 */
@Entity
@Table(name = "brands")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    /**
     * The unique identifier for the brand.
     * Primary Key, auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The official display name of the brand.
     * <p>
     * Example: "Yonex", "Victor".
     * This field is mandatory for data integrity.
     * </p>
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * The URL or path to the brand's logo image.
     * <p>
     * Can be a link to cloud storage (AWS S3, Cloudinary) or a local path.
     * Displayed on the brand carousel or product details page.
     * </p>
     */
    @Column
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ========================================================================
    // RELATIONSHIPS
    // ========================================================================

    /**
     * The list of products manufactured by this brand.
     * <p>
     * - @JsonIgnore: Prevents the entire list of products from being serialized
     * when fetching a simple list of brands (Performance optimization & prevents recursion).
     * - @Builder.Default: Ensures the list is initialized as an empty ArrayList
     * instead of null when using the Builder pattern.
     * </p>
     */
    @OneToMany(mappedBy = "brand")
    @JsonIgnore
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}