package com.badmintonshop.shared.entity.enums;

/**
 * Represents the current validity status of an authentication Token.
 * <p>
 * This enum replaces the legacy {@code revoked} and {@code expired} boolean fields
 * to provide a single, comprehensive representation of token status.
 * </p>
 */
public enum TokenStatus {
    /**
     * Token is valid and can be used for authentication.
     */
    VALID,

    /**
     * Token has been explicitly revoked (logged out) and cannot be used.
     */
    REVOKED,

    /**
     * Token has expired and is no longer valid for authentication.
     */
    EXPIRED
}
