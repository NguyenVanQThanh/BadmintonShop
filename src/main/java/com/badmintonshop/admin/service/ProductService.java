package com.badmintonshop.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badmintonshop.admin.dto.ProductRequest;
import com.badmintonshop.admin.payload.response.BrandResponse;
import com.badmintonshop.admin.payload.response.CategoryResponse;
import com.badmintonshop.admin.payload.response.ProductResponse;
import com.badmintonshop.admin.payload.response.ProductVariantResponse;
import com.badmintonshop.shared.entity.Brand;
import com.badmintonshop.shared.entity.Category;
import com.badmintonshop.shared.entity.Product;
import com.badmintonshop.shared.entity.ProductVariant;
import com.badmintonshop.shared.entity.enums.ProductStatus;
import com.badmintonshop.shared.exception.ResourceNotFoundException;
import com.badmintonshop.shared.repository.BrandRepository;
import com.badmintonshop.shared.repository.CategoryRepository;
import com.badmintonshop.shared.repository.ProductRepository;
import com.badmintonshop.shared.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Service class responsible for managing Product catalog operations.
 * <p>
 * Handles creation, retrieval of Products, Brands, and Categories.
 * Interacts with {@link ProductRepository}, {@link BrandRepository},
 * and {@link CategoryRepository}.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Retrieves a paginated list of all products regardless of status.
     * <p>
     * Intended for Admin use to see the full product catalog including
     * discontinued or out-of-stock items.
     * </p>
     *
     * @param pageable Pagination information (page number, size, sort).
     * @return A {@link Page} of {@link ProductResponse} DTOs.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toProductResponse);
    }

    /**
     * Retrieves a paginated list of active products (IN_STOCK or COMING_SOON).
     * <p>
     * Intended for Cashier use to display only purchasable or upcoming products.
     * </p>
     *
     * @param pageable Pagination information (page number, size, sort).
     * @return A {@link Page} of {@link ProductResponse} DTOs.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        return productRepository
                .findByStatusIn(List.of(ProductStatus.IN_STOCK, ProductStatus.COMING_SOON), pageable)
                .map(this::toProductResponse);
    }

    /**
     * Creates a new product in the catalog.
     * <p>
     * Logic:
     * 1. Validates that the referenced Brand exists.
     * 2. Validates that the referenced Category exists.
     * 3. Persists the new Product entity.
     * </p>
     *
     * @param request The payload containing the new product's details.
     * @return The created {@link ProductResponse} DTO.
     * @throws ResourceNotFoundException if Brand or Category is not found.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus())
                .brand(brand)
                .category(category)
                .build();

        return toProductResponse(productRepository.save(product));
    }

    /**
     * Retrieves a list of all brands.
     * <p>
     * Intended for use in product creation/edit forms to populate the Brand dropdown.
     * </p>
     *
     * @return List of all {@link BrandResponse} DTOs.
     */
    @Transactional(readOnly = true)
    public List<BrandResponse> getBrands() {
        return brandRepository.findAll().stream()
                .map(this::toBrandResponse)
                .toList();
    }

    /**
     * Retrieves a list of all categories.
     * <p>
     * Intended for use in product creation/edit forms to populate the Category dropdown.
     * </p>
     *
     * @return List of all {@link CategoryResponse} DTOs.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    // ========================================================================
    // MAPPERS
    // ========================================================================

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .status(product.getStatus().name())
                .brand(toBrandResponse(product.getBrand()))
                .category(toCategoryResponse(product.getCategory()))
                .variants(product.getVariants().stream().map(this::toVariantResponse).toList())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .imageUrl(variant.getImageUrl())
                .attributes(variant.getAttributes())
                .status(variant.getStatus().name())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }

    private BrandResponse toBrandResponse(Brand brand) {
        if (brand == null) return null;
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logoUrl(brand.getLogoUrl())
                .status(brand.getStatus().name())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .status(category.getStatus().name())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
