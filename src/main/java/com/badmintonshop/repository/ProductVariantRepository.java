package com.badmintonshop.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.badmintonshop.entity.ProductVariant;

/**
 * Repository for managing ProductVariant entities.
 * Handles standard CRUD operations, complex JSONB attribute filtering, and atomic stock management.
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // =========================================================================
    // STANDARD JPA QUERIES
    // =========================================================================

    /**
     * Retrieves a variant by its unique SKU.
     *
     * @param sku The Stock Keeping Unit identifier.
     * @return An Optional containing the variant if found, or empty otherwise.
     */
    Optional<ProductVariant> findBySku(String sku);

    /**
     * Finds all variants belonging to a specific product with pagination.
     *
     * @param productId The ID of the parent product.
     * @param pageable  Pagination information (page number, size, sorting).
     * @return A Page of ProductVariant.
     */
    Page<ProductVariant> findByProductId(Long productId, Pageable pageable);

    /**
     * Finds variants within a specific price range.
     *
     * @param minPrice The minimum price (inclusive).
     * @param maxPrice The maximum price (inclusive).
     * @param pageable Pagination information.
     * @return A Page of ProductVariant falling within the price range.
     */
    Page<ProductVariant> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // =========================================================================
    // JSONB NATIVE QUERIES (PostgreSQL Specific)
    // =========================================================================

    /**
     * Filters variants by the 'color' attribute stored in the JSONB column.
     *
     * @param color    The color value to search for.
     * @param pageable Pagination information.
     * @return A Page of matching ProductVariant.
     */
    @Query(value = "SELECT * FROM product_variants WHERE attributes ->> 'color' = :color",
           countQuery = "SELECT count(*) FROM product_variants WHERE attributes ->> 'color' = :color",
           nativeQuery = true)
    Page<ProductVariant> findByColor(@Param("color") String color, Pageable pageable);

    /**
     * Filters SHOE type variants by 'size'.
     * Casts the JSON text value to NUMERIC for accurate numerical comparison.
     *
     * @param size     The shoe size to search for.
     * @param pageable Pagination information.
     * @return A Page of matching ProductVariant.
     */
    @Query(value = "SELECT * FROM product_variants WHERE attributes ->> 'type' = 'SHOE' " +
                   "AND CAST(attributes ->> 'size' AS NUMERIC) = :size",
           countQuery = "SELECT count(*) FROM product_variants WHERE attributes ->> 'type' = 'SHOE' " +
                        "AND CAST(attributes ->> 'size' AS NUMERIC) = :size",
           nativeQuery = true)
    Page<ProductVariant> findShoeBySize(@Param("size") Double size, Pageable pageable);

    /**
     * Filters RACKET type variants by 'weight' (e.g., '3U', '4U').
     *
     * @param weight   The weight classification.
     * @param pageable Pagination information.
     * @return A Page of matching ProductVariant.
     */
    @Query(value = "SELECT * FROM product_variants WHERE attributes ->> 'type' = 'RACKET' " +
                   "AND attributes ->> 'weight' = :weight",
           countQuery = "SELECT count(*) FROM product_variants WHERE attributes ->> 'type' = 'RACKET' " +
                        "AND attributes ->> 'weight' = :weight",
           nativeQuery = true)
    Page<ProductVariant> findRacketByWeight(@Param("weight") String weight, Pageable pageable);

    /**
     * Filters SHOE type variants by 'gender' (e.g., 'MEN', 'WOMEN', 'UNISEX').
     *
     * @param gender   The target gender.
     * @param pageable Pagination information.
     * @return A Page of matching ProductVariant.
     */
    @Query(value = "SELECT * FROM product_variants WHERE attributes ->> 'type' = 'SHOE' " +
                   "AND attributes ->> 'gender' = :gender",
           countQuery = "SELECT count(*) FROM product_variants WHERE attributes ->> 'type' = 'SHOE' " +
                        "AND attributes ->> 'gender' = :gender",
           nativeQuery = true)
    Page<ProductVariant> findShoeByGender(@Param("gender") String gender, Pageable pageable);

    /**
     * Filters RACKET type variants by 'stiffness' (e.g., 'FLEXIBLE', 'STIFF').
     *
     * @param stiffness The stiffness level.
     * @param pageable  Pagination information.
     * @return A Page of matching ProductVariant.
     */
    @Query(value = "SELECT * FROM product_variants WHERE attributes ->> 'type' = 'RACKET' " +
                   "AND attributes ->> 'stiffness' = :stiffness",
           countQuery = "SELECT count(*) FROM product_variants WHERE attributes ->> 'type' = 'RACKET' " +
                        "AND attributes ->> 'stiffness' = :stiffness",
           nativeQuery = true)
    Page<ProductVariant> findRacketByStiffness(@Param("stiffness") String stiffness, Pageable pageable);

    // =========================================================================
    // INVENTORY MANAGEMENT (Concurrency Safe)
    // =========================================================================

    /**
     * Retrieves only the stock quantity for a given SKU.
     * This is a lightweight projection query to avoid fetching the entire entity.
     *
     * @param sku The Stock Keeping Unit.
     * @return Optional containing the stock quantity.
     */
    @Query("SELECT pv.stockQuantity FROM ProductVariant pv WHERE pv.sku = :sku")
    Optional<Integer> getStockBySku(@Param("sku") String sku);

    /**
     * Retrieves only the stock quantity for a given ID.
     *
     * @param id The variant ID.
     * @return Optional containing the stock quantity.
     */
    @Query("SELECT pv.stockQuantity FROM ProductVariant pv WHERE pv.id = :id")
    Optional<Integer> getStockById(@Param("id") Long id);

    /**
     * Atomically decreases the stock quantity.
     * Performs a DB-level check (stock >= amount) to prevent negative inventory (overselling).
     * <p>
     * Note: This method clears the Hibernate persistence context automatically
     * to ensure subsequent fetches retrieve the updated data.
     *
     * @param id     The ID of the product variant.
     * @param amount The quantity to deduct.
     * @return 1 if the update was successful (sufficient stock), 0 otherwise.
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity - :amount " +
           "WHERE pv.id = :id AND pv.stockQuantity >= :amount")
    int decreaseStock(@Param("id") Long id, @Param("amount") int amount);

    /**
     * Increases the stock quantity (e.g., for restocking or order cancellations).
     *
     * @param id     The ID of the product variant.
     * @param amount The quantity to add.
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity + :amount " +
           "WHERE pv.id = :id")
    void increaseStock(@Param("id") Long id, @Param("amount") int amount);
}