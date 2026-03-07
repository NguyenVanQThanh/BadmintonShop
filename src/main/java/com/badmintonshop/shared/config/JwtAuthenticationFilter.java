package com.badmintonshop.shared.config;

import com.badmintonshop.shared.repository.TokenRepository;
import com.badmintonshop.shared.security.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that executes once per request to validate the JWT Token.
 * <p>
 * This filter sits before the default Spring Security filters.
 * It checks the "Authorization" header for a Bearer token.
 * If valid, it sets the user authentication in the SecurityContext.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Pass to next filter (no auth)
            return;
        }

        // 2. Extract JWT Token
        jwt = authHeader.substring(7); // Remove "Bearer " prefix

        // 3. Extract Username (Email) from Token
        // NOTE: Ensure your JwtUtils has extractUsername() method
        try {
            userEmail = jwtUtils.extractUsername(jwt);
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 4. If user is found and not already authenticated in this context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            var isTokenValid = tokenRepository.findByToken(jwt)
            .map(t -> t.getStatus() == com.badmintonshop.shared.entity.enums.TokenStatus.VALID)
            .orElse(false);
            // 5. Load UserDetails from Database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Validate Token
            if (jwtUtils.isTokenValid(jwt, userDetails) && isTokenValid) {
                
                // 7. Create Authentication Token (Standard Spring Security Object)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                
                // 8. Enforce request details (IP, Session ID, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Update SecurityContextHolder -> User is now "Logged In" for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 10. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}