package com.badmintonshop.shared.entity.enums;

/**
 * Represents the current inventory and availability status of a Product.
 * <p>
 * This enum replaces the legacy {@code isActive} boolean field to provide more granular
 * control over product visibility and availability states.
 * </p>
 */
public enum ProductStatus {
    /**
     * Product is available for purchase and visible in the storefront.
     */
    IN_STOCK,

    /**
     * Product is temporarily out of stock but may become available again.
     */
    OUT_OF_STOCK,

    /**
     * Product is discontinued and will not be restocked.
     */
    DISCONTINUED,

    /**
     * Product is not yet available but will be released in the future.
     */
    COMING_SOON
}
