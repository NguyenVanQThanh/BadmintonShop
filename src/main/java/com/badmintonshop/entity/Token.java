package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.TokenType;

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
     * Flag indicating if the token has been manually revoked (e.g., via Logout).
     */
    private boolean revoked;

    /**
     * Flag indicating if the token has naturally expired (time passed).
     */
    private boolean expired;

    /**
     * The user who owns this token.
     * <p>
     * FetchType.LAZY is used for performance. The employee data is only loaded
     * when explicitly accessed.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude // CRITICAL: Prevents LazyInitializationException during logging
    private Account account;
}