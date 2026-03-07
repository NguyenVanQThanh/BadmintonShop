package com.badmintonshop.shared.config;

import com.badmintonshop.shared.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import java.lang.module.ModuleDescriptor.Provides;

import org.apache.poi.sl.usermodel.ObjectMetaData.Application;
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

    private final AccountRepository accountRepository;

    /**
     * Defines the logic to retrieve user details from the database.
     * Used by Spring Security during authentication.
     *
     * @return UserDetailsService implementation.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> accountRepository.findByEmail(username)
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
    
    /**
     * Provides the primary {@link ObjectMapper} bean for the Spring Application Context.
     * <p>
     * <b>Role:</b> The ObjectMapper is the core component of the Jackson library,
     * responsible for data binding (converting JSON to Java Objects and vice versa).
     * It is essential for processing JSON data in the {@code ExcelDataSeeder} 
     * and handling JSONB columns in entities.
     * </p>
     * <p>
     * <b>Configuration Note:</b> Explicitly defining this bean guarantees its availability 
     * for Dependency Injection, preventing "Parameter required a bean of type ObjectMapper" 
     * errors during application startup.
     * </p>
     *
     * @return A standard, fresh instance of {@link ObjectMapper}.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}