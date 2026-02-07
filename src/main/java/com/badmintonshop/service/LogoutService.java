package com.badmintonshop.service;

import com.badmintonshop.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

/**
 * Service responsible for handling the logout process.
 * <p>
 * This service implements Spring Security's {@link LogoutHandler}.
 * It is automatically invoked when the client calls the configured logout endpoint.
 * Key responsibilities:
 * <ul>
 * <li>Extracting the JWT from the Authorization header.</li>
 * <li>Marking the token as expired and revoked in the database.</li>
 * <li>Clearing the SecurityContext to invalidate the current session.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    /**
     * Performs the logout logic.
     *
     * @param request        The HTTP request.
     * @param response       The HTTP response.
     * @param authentication The current authentication object (may be null if already logged out).
     */
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 1. Validate the Authorization Header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Logout request ignored: Missing or invalid Authorization header.");
            return;
        }

        // 2. Extract JWT Token
        jwt = authHeader.substring(7);

        // 3. Find the token in the database
        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);

        // 4. Invalidate the token
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            
            // Clear the security context explicitly
            SecurityContextHolder.clearContext();
            
            log.info("Logout successful. Token revoked for user ID: {}", storedToken.getAccount().getId());
        } else {
            log.warn("Logout attempt failed: Token not found in database or already deleted.");
        }
    }
}