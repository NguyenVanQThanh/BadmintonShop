package com.badmintonshop.repository;

import com.badmintonshop.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link ProductVariant} entities (SKUs).
 * <p>
 * Handles operations related to specific product items, inventory checks,
 * price filtering, and PostgreSQL JSONB attribute queries.
 * </p>
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // ========================================================================
    // SKU VALIDATION & LOOKUP
    // ========================================================================

    /**
     * Checks if a variant with the given SKU already exists.
     * <p>
     * <b>Use Case:</b> Excel Import validation to prevent duplicate SKUs.
     * </p>
     *
     * @param sku The Stock Keeping Unit code to check.
     * @return {@code true} if exists, {@code false} otherwise.
     */
    boolean existsBySku(String sku);

    /**
     * Retrieves a specific variant by its SKU.
     * <p>
     * <b>Use Case:</b> Cart/Order processing where SKU is the reference key.
     * </p>
     *
     * @param sku The unique SKU.
     * @return Optional containing the variant.
     */
    Optional<ProductVariant> findBySku(String sku);

    // ========================================================================
    // DISPLAY & LISTING
    // ========================================================================

    /**
     * Retrieves all variants belonging to a specific parent product.
     * <p>
     * <b>Use Case:</b> Displaying product details page (e.g., showing 3U, 4U versions).
     * </p>
     *
     * @param productId The ID of the parent product.
     * @return List of all variants (including out-of-stock ones).
     */
    List<ProductVariant> findByProductId(Long productId);

    /**
     * Retrieves only available (in-stock) variants for a product.
     * <p>
     * <b>Use Case:</b> Customer view - Hide options that are out of stock (Optional).
     * </p>
     */
    List<ProductVariant> findByProductIdAndStockQuantityGreaterThan(Long productId, Integer quantity);

    // ========================================================================
    // ADVANCED FILTERING (JSONB & PRICE)
    // ========================================================================

    /**
     * Finds variants matching a specific JSON attribute (PostgreSQL Specific).
     * <p>
     * <b>Use Case:</b> "Find all rackets with 4U weight" or "Find shoes with Size 40".
     * Uses native SQL to query the JSONB column efficiently.
     * </p>
     *
     * @param key   The JSON key (e.g., "weight", "size").
     * @param value The JSON value to match (e.g., "4U", "40").
     * @return List of matching variants.
     */
    @Query(value = "SELECT * FROM product_variants v WHERE v.attributes ->> :key = :value", nativeQuery = true)
    List<ProductVariant> findByAttribute(@Param("key") String key, @Param("value") String value);

    /**
     * Filters variants within a specific price range.
     * <p>
     * <b>Use Case:</b> Search filter "Price: 1M - 3M VND".
     * Returns a Page to handle large result sets efficiently.
     * </p>
     *
     * @param minPrice The minimum price.
     * @param maxPrice The maximum price.
     * @param pageable Pagination info.
     * @return Page of variants within range.
     */
    Page<ProductVariant> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}