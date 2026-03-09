package com.badmintonshop.admin.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.badmintonshop.admin.dto.ProductRequest;
import com.badmintonshop.admin.payload.response.BrandResponse;
import com.badmintonshop.admin.payload.response.CategoryResponse;
import com.badmintonshop.admin.payload.response.ProductResponse;
import com.badmintonshop.admin.service.ProductService;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * REST Controller for managing Product catalog resources.
 * <p>
 * Provides endpoints for product listing, creation, and supporting data
 * such as Brands and Categories used in product forms.
 * Access is controlled at the method level via {@link PreAuthorize} annotations:
 * - Read endpoints require CASHIER role (ADMIN inherits this via Role Hierarchy)
 * - Write endpoints require ADMIN role only
 * </p>
 *
 * @see ProductService
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    /**
     * Retrieves a paginated list of all products regardless of status.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/admin/products
     * </p>
     *
     * @param pageable Pagination parameters (page, size, sort). Default size: 20.
     * @return A ResponseEntity containing a {@link Page} of {@link ProductResponse} DTOs.
     */
    @GetMapping
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProducts(pageable));
    }

    /**
     * Creates a new product in the catalog.
     * <p>
     * The request body is validated against {@link ProductRequest} constraints.
     * Upon success, returns HTTP 201 (Created).
     * HTTP Method: POST
     * Endpoint: /api/admin/products
     * </p>
     *
     * @param request The payload containing the new product's details.
     * @return A ResponseEntity containing the created {@link ProductResponse} and HTTP status 201.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    /**
     * Retrieves a list of all brands.
     * <p>
     * Intended for populating the Brand dropdown in product creation/edit forms.
     * HTTP Method: GET
     * Endpoint: /api/admin/products/brands
     * </p>
     *
     * @return A ResponseEntity containing the list of {@link BrandResponse} DTOs.
     */
    @GetMapping("/brands")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<List<BrandResponse>> getBrands() {
        return ResponseEntity.ok(productService.getBrands());
    }

    /**
     * Retrieves a list of all categories.
     * <p>
     * Intended for populating the Category dropdown in product creation/edit forms.
     * HTTP Method: GET
     * Endpoint: /api/admin/products/categories
     * </p>
     *
     * @return A ResponseEntity containing the list of {@link CategoryResponse} DTOs.
     */
    @GetMapping("/categories")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }
}
