package com.badmintonshop.entity.enums;
/**
 * Enumeration of standard role names used in the RBAC system.
 * <p>
 * These names correspond to specific actions that can be granted to roles.
 * Examples: "ADMIN", "CASHIER"
 * </p>
 */
public enum RoleName {
    ADMIN
    , CASHIER;

    public static boolean isValidRole(RoleName role) {
        for (RoleName r : RoleName.values()) {
            if (r == role) {
                return true;
            }
        }
        return false;
    }
}
