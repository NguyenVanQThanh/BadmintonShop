package com.badmintonshop.config;

import com.badmintonshop.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Main Application Configuration.
 * <p>
 * This class defines application-wide beans such as:
 * <ul>
 * <li>PasswordEncoder (BCrypt)</li>
 * <li>UserDetailsService (Database lookup)</li>
 * <li>AuthenticationManager</li>
 * </ul>
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final EmployeeRepository employeeRepository; // Repository của bạn

    /**
     * Defines the logic to retrieve user details from the database.
     * Used by Spring Security during authentication.
     *
     * @return UserDetailsService implementation.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> employeeRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /**
     * Defines the Data Access Object (DAO) Authentication Provider.
     * It connects the UserDetailsService with the PasswordEncoder.
     *
     * @return The configured AuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager bean, which is required by the AuthService
     * to perform the actual login process.
     *
     * @param config The injected AuthenticationConfiguration.
     * @return The AuthenticationManager instance.
     * @throws Exception If configuration fails.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the Password Encoder using BCrypt Hashing Algorithm.
     * <p>
     * This is the standard for secure password storage.
     * The DataSeeder will use this to hash the initial Admin password.
     * </p>
     *
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}