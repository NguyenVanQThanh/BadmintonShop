package com.badmintonshop.shared.entity.enums;

/**
 * Represents the current status of an Account in the system.
 * <p>
 * This enum replaces the legacy {@code enabled} boolean field to provide more granular
 * control over account lifecycle and security states.
 * </p>
 */
public enum AccountStatus {
    /**
     * Account is active and the user can log in and perform operations.
     */
    ACTIVE,

    /**
     * Account is inactive (user chose to disable it or was deactivated by admin).
     * The user cannot log in.
     */
    INACTIVE,

    /**
     * Account is temporarily suspended, typically due to security concerns or policy violations.
     * The account may be reactivated after a review period.
     */
    SUSPENDED,

    /**
     * Account is permanently banned and cannot be used or reactivated.
     * Used for serious violations or violations of terms of service.
     */
    BANNED
}
