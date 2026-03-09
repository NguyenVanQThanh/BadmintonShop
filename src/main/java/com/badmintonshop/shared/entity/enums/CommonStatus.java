package com.badmintonshop.shared.entity.enums;

/**
 * General-purpose status enum for catalog entities (Brand, Category, ProductVariant).
 * <p>
 * Used to control visibility and availability without needing entity-specific states.
 * </p>
 */
public enum CommonStatus {
    /**
     * Entity is active and visible on the storefront.
     */
    ACTIVE,

    /**
     * Entity is inactive and hidden from the storefront.
     */
    INACTIVE
}
