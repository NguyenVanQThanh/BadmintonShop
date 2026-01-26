package com.badmintonshop.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.badmintonshop.entity.Employee;
import com.badmintonshop.entity.RefreshToken;

/**
 * Repository for managing RefreshToken entities.
 * Includes mechanisms for token validation, revocation, and automated cleanup.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its string value.
     * Used during the token rotation/refresh flow.
     *
     * @param token The token string.
     * @return An Optional containing the RefreshToken if found.
     */
    Optional<RefreshToken> findByToken(String token);

    // =========================================================================
    // REVOCATION (LOGOUT)
    // =========================================================================

    /**
     * Deletes all refresh tokens associated with a specific employee.
     * Useful for "Logout All Devices" functionality or when an employee is blocked.
     *
     * @param employee The employee entity.
     * @return The number of tokens deleted.
     */
    @Transactional
    @Modifying
    int deleteByEmployee(Employee employee);
    
    /**
     * Deletes a specific refresh token.
     * Used for standard "Logout" on a single device.
     * * @param token The token string to revoke.
     */
    @Transactional
    @Modifying
    void deleteByToken(String token);

    // =========================================================================
    // MAINTENANCE & CLEANUP
    // =========================================================================

    /**
     * Deletes all refresh tokens that have expired before the given point in time.
     * This method is intended to be called by a Scheduled Task (Cron Job) 
     * to prevent the database from being cluttered with dead tokens.
     *
     * @param now The current timestamp (Instant).
     * @return The number of expired tokens deleted.
     */
    @Transactional
    @Modifying
    int deleteByExpiryDateBefore(Instant now);
}