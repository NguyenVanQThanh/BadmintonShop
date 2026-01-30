package com.badmintonshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility component for JSON Web Token (JWT) management.
 * <p>
 * This class handles:
 * <ul>
 * <li>Token Generation (Signing)</li>
 * <li>Token Parsing & Claim Extraction</li>
 * <li>Token Validation (Expiration & Signature checks)</li>
 * </ul>
 * It uses the HMAC SHA-256 algorithm for signing.
 * </p>
 */
@Component
public class JwtUtils {

    /**
     * The secret key used to sign the JWT.
     * Must be at least 256 bits (32 characters) and Base64 encoded in application.properties.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * The expiration time of the token in milliseconds.
     */
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // ========================================================================
    // 1. TOKEN GENERATION
    // ========================================================================

    /**
     * Generates a JWT token for the authenticated user.
     *
     * @param authentication The Spring Security Authentication object.
     * @return A signed JWT string.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateToken(new HashMap<>(), userPrincipal);
    }

    /**
     * Helper method to build the JWT with extra claims.
     *
     * @param extraClaims Additional custom claims (e.g., role, user ID) to embed in the token.
     * @param userDetails The user details containing the username (subject).
     * @return A signed JWT string.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // Sets the "sub" claim (Email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Sets the "iat" claim
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Sets the "exp" claim
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Signs the token
                .compact();
    }

    // ========================================================================
    // 2. DATA EXTRACTION
    // ========================================================================

    /**
     * Extracts the Username (Subject) from the JWT token.
     *
     * @param token The JWT string.
     * @return The username (email) stored in the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the Expiration Date from the JWT token.
     *
     * @param token The JWT string.
     * @return The expiration date.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract a specific claim from the token.
     *
     * @param token          The JWT string.
     * @param claimsResolver A function to resolve the desired claim from the Claims object.
     * @param <T>            The type of the claim being extracted.
     * @return The extracted claim value.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT token to retrieve all claims (Payload).
     * <p>
     * This method validates the signature internally. If the signature is invalid
     * or the token is malformed, it will throw a RuntimeException (handled by JwtFilter).
     * </p>
     *
     * @param token The JWT string.
     * @return The Claims object containing payload data.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ========================================================================
    // 3. TOKEN VALIDATION
    // ========================================================================

    /**
     * Validates the JWT token against the UserDetails from the database.
     *
     * @param token       The JWT string.
     * @param userDetails The user details loaded from the database.
     * @return true if the token belongs to the user and is not expired; false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Valid if: Username matches AND Token is not expired
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the token has expired.
     *
     * @param token The JWT string.
     * @return true if the current time is after the expiration time.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ========================================================================
    // 4. UTILITIES
    // ========================================================================

    /**
     * Decodes the Secret Key from Base64 and creates a Cryptographic Key object.
     *
     * @return The Key object for HMAC-SHA algorithms.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}