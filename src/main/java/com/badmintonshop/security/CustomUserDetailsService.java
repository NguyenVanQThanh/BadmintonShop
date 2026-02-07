package com.badmintonshop.security;

import com.badmintonshop.entity.Account;
import com.badmintonshop.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * <p>
 * This service bridges the gap between the application's database (Employee entity)
 * and Spring Security's internal authentication mechanism (UserDetails).
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    /**
     * Loads the user details by the given username (email in this context).
     *
     * @param email The email of the user attempting to authenticate.
     * @return A UserDetails object containing the user's credentials and authorities.
     * @throws UsernameNotFoundException If no user is found with the provided email.
     */
    @Override
    @Transactional(readOnly = true) // Optimization: Tells JPA this transaction is read-only
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Attempting to load user details for email: {}", email);

        // 1. Retrieve the employee from the database
        Account employee = accountRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // 2. Convert the Employee's Role into a Spring Security Authority
        // Example: RoleName.ROLE_ADMIN is converted to the String "ROLE_ADMIN"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(employee.getRole().name());

        log.debug("User found: {}. Assigning authority: {}", email, authority.getAuthority());

        // 3. Return the fully populated UserDetails object
        // This object is used by the AuthenticationManager to verify the password
        return new User(
                employee.getEmail(),
                employee.getPassword(),
                Collections.singletonList(authority) // List of granted authorities
        );
    }
}