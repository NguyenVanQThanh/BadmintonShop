package com.badmintonshop.shared.entity;

import com.badmintonshop.shared.entity.enums.TokenType;
import com.badmintonshop.shared.entity.enums.TokenStatus;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a JWT Token stored in the database.
 * <p>
 * This entity allows the application to track active tokens, enabling
 * features like "Force Logout" or detecting token theft/reuse.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique JWT string.
     * <p>
     * Note: JWTs can be long, so we define a larger column length (2048)
     * instead of the default 255 to prevent DataTruncation errors.
     * </p>
     */
    @Column(unique = true, nullable = false, length = 2048)
    private String token;

    /**
     * The type of the token (e.g., BEARER).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default // Ensures the builder uses this default value if not provided
    private TokenType tokenType = TokenType.BEARER;

    /**
     * The current status of the token.
     * <p>
     * Uses {@link TokenStatus} enum to represent different states:
     * - VALID: Token is valid and can be used
     * - REVOKED: Token has been manually revoked (e.g., logout)
     * - EXPIRED: Token has naturally expired
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TokenStatus status = TokenStatus.VALID;

    /**
     * The user who owns this token.
     * <p>
     * FetchType.LAZY is used for performance. The employee data is only loaded
     * when explicitly accessed.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    @ToString.Exclude // CRITICAL: Prevents LazyInitializationException during logging
    private Account account;
}