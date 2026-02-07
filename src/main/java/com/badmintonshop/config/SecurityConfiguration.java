package com.badmintonshop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Main Security Configuration for the application.
 * <p>
 * This class configures the Security Filter Chain, including:
 * <ul>
 * <li>CORS (Cross-Origin Resource Sharing) policies.</li>
 * <li>CSRF disabling (since we use stateless JWT).</li>
 * <li>Session management (Stateless).</li>
 * <li>URL protection rules (RBAC).</li>
 * <li>JWT Filter integration.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    /**
     * Configures the HTTP Security Filter Chain.
     *
     * @param http The HttpSecurity object to configure.
     * @return The built SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CORS Configuration: Enable CORS with custom settings defined in corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. CSRF (Cross-Site Request Forgery): Disabled because we are using stateless JWT authentication.
            // CSRF protection is typically required for session-based (cookie) auth, but not for header-based auth.
            .csrf(AbstractHttpConfigurer::disable)
            
            // 3. Authorization Rules: Define which endpoints are public and which are protected.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Allow unauthenticated access to Login/Register
                .requestMatchers("/api/admin/employees/**").hasRole("ADMIN")
                .requestMatchers("/api/employee/**").hasAnyRole("ADMIN", "MANAGER", "STAFF", "WAREHOUSE")
                .anyRequest().authenticated() // Require authentication for all other endpoints
            )
            
            // 4. Session Management: Configure the application to be Stateless.
            // Spring Security will not create or use any HTTP sessions. Every request must contain the JWT.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 5. Authentication Provider: Set the custom authentication logic (UserDetailsService + PasswordEncoder).
            .authenticationProvider(authenticationProvider)
            
            // 6. Filter Chain: Add the JwtAuthenticationFilter BEFORE the standard UsernamePasswordAuthenticationFilter.
            // This ensures that the Token is checked before trying to process a form login.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // 7. Logout Configuration: Define the logout URL and the custom logout handler.
            .logout(logout -> logout
                .logoutUrl("/api/employee/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication)-> SecurityContextHolder.clearContext())
            );

        return http.build();
    }

    // ========================================================================
    // PRODUCTION CORS CONFIGURATION
    // ========================================================================

    /**
     * Defines the CORS (Cross-Origin Resource Sharing) configuration.
     * <p>
     * This bean controls which domains are allowed to access the API.
     * In a production environment, strictly limit the allowed origins.
     * </p>
     *
     * @return The CorsConfigurationSource object.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Allowed Origins: Which domains can call this API?
        // IMPORTANT: In Production, replace "localhost" with your actual frontend domain (e.g., https://badmintonshop.com).
        // Using "*" (Wildcard) is NOT recommended for security, especially when allowCredentials is true.
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000"  // Local React/Next.js
            , "http://localhost:5173"  // Local Vite/Vue
            // , "https://badmintonshop.com" // Production Domain
            // , "https://admin.badmintonshop.com" // Admin Portal Domain
        ));

        // 2. Allowed Methods: Which HTTP methods are permitted?
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 3. Allowed Headers: Which headers can be sent in the request?
        // "Authorization" is required for sending the Bearer Token.
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));

        // 4. Allow Credentials: Should the browser send cookies/auth headers?
        // Must be true if the frontend needs to send/receive cookies or authorization headers across domains.
        configuration.setAllowCredentials(true);

        // 5. Max Age: How long should the browser cache the CORS "Pre-flight" (OPTIONS) response?
        // 3600 seconds (1 hour) reduces the number of pre-flight requests to the server.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all endpoints ("/**") in the application.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}