package com.badmintonshop.shared.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.badmintonshop.shared.entity.Product;
import com.badmintonshop.shared.entity.enums.ProductStatus;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for managing the general {@link Product} catalog.
 * <p>
 * This repository handles operations related to the parent product entity,
 * allowing for broad searching and filtering capabilities across the store.
 * </p>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Retrieves a product by its exact name.
     * <p>
     * <b>Use Case:</b> Data Seeding or Admin checks to prevent duplicate product names.
     * </p>
     */
    Optional<Product> findByName(String name);

    /**
     * Performs a case-insensitive search on product names.
     * <p>
     * <b>Use Case:</b> The main Search Bar functionality on the storefront.
     * Only returns products with IN_STOCK or COMING_SOON status.
     * </p>
     *
     * @param keyword  The search term entered by the user.
     * @param pageable Pagination information (page number, size, sort).
     * @return A {@link Page} of active products matching the keyword.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.status IN ('IN_STOCK', 'COMING_SOON')")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Finds available products belonging to a specific category (via Slug).
     * <p>
     * <b>Use Case:</b> Listing products when a user clicks a category menu item.
     * Uses JOIN FETCH to optimize performance by loading the category in one query.
     * Only returns products with IN_STOCK or COMING_SOON status.
     * </p>
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.category c WHERE c.slug = :slug AND p.status IN ('IN_STOCK', 'COMING_SOON')")
    Page<Product> findByCategorySlug(@Param("slug") String slug, Pageable pageable);

    /**
     * Finds available products belonging to a specific brand.
     * <p>
     * <b>Use Case:</b> Listing products when a user filters by Brand (e.g., "Show all Yonex").
     * </p>
     */
    Page<Product> findByBrandIdAndStatusIn(Long brandId, List<ProductStatus> statuses, Pageable pageable);

    /**
     * Retrieves all available products with pagination.
     * <p>
     * <b>Use Case:</b> Displaying all available products on the storefront with pagination support.
     * Only returns products with IN_STOCK or COMING_SOON status.
     * </p>
     *
     * @param pageable Pagination information (page number, size, sort).
     * @return A {@link Page} of all available products.
     */
    Page<Product> findByStatusIn(List<ProductStatus> statuses, Pageable pageable);
}