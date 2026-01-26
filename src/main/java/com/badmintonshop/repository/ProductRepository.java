package com.badmintonshop.repository;

import com.badmintonshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * Retrieves a list of products belonging to a specific manufacturer.
     * <p>
     * Used for "Brand Filtering" on the storefront sidebar.
     * Example: {@code findByBrand("Yonex")} returns all Yonex rackets, shoes, etc.
     * </p>
     *
     * @param brand The brand name to filter by (case-sensitive by default).
     * @return A list of matching products.
     */
    List<Product> findByBrand(String brand);

    /**
     * Performs a case-insensitive "fuzzy" search on the product name.
     * <p>
     * This method translates to a SQL {@code LIKE %name%} query, allowing users
     * to find products even if they only type a partial keyword.
     * <br>
     * <b>Example:</b> Input "astrox" will match "Yonex Astrox 100ZZ".
     * </p>
     *
     * @param name The search keyword input by the user.
     * @return A list of products containing the keyword in their name.
     */
    List<Product> findByNameContainingIgnoreCase(String name);
}