package com.badmintonshop.entity.enums;

/**
 * Enumeration of all available permissions in the system.
 * <p>
 * Naming Convention: RESOURCE_ACTION
 * This ensures granular control over specific resources.
 * </p>
 */
public enum PermissionName {

    // --- PRODUCT MANAGEMENT ---
    PRODUCT_READ
    , PRODUCT_CREATE
    , PRODUCT_UPDATE
    , PRODUCT_DELETE

    // --- ORDER MANAGEMENT ---
    , ORDER_READ
    , ORDER_CREATE
    , ORDER_UPDATE
    , ORDER_DELETE

    // --- INVENTORY / WAREHOUSE MANAGEMENT ---
    , INVENTORY_READ
    , INVENTORY_IMPORT
    , INVENTORY_EXPORT

    // --- CUSTOMER MANAGEMENT ---
    , CUSTOMER_READ
    , CUSTOMER_CREATE
    , CUSTOMER_UPDATE

    // --- EMPLOYEE MANAGEMENT (Internal Users) ---
    , EMPLOYEE_READ
    , EMPLOYEE_CREATE
    , EMPLOYEE_UPDATE
    , EMPLOYEE_DELETE

    // --- SYSTEM / REPORTS ---
    , DASHBOARD_VIEW
    , REPORT_VIEW
}