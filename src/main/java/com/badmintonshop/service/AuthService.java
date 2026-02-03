package com.badmintonshop.service;

import com.badmintonshop.entity.Employee;
import com.badmintonshop.entity.Token;
import com.badmintonshop.entity.enums.TokenType;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.payload.request.LoginRequest;
import com.badmintonshop.payload.response.AuthResponse;
import com.badmintonshop.repository.EmployeeRepository;
import com.badmintonshop.repository.TokenRepository;
import com.badmintonshop.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for handling core Authentication and Authorization logic.
 * <p>
 * Key responsibilities:
 * <ul>
 * <li>Verifying user credentials via {@link AuthenticationManager}.</li>
 * <li>Managing JWT lifecycles (Generation, Persistence, Revocation).</li>
 * <li>Enforcing security policies (e.g., Single Session per user).</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenRepository tokenRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Authenticates a user based on email and password.
     * <p>
     * If authentication is successful:
     * 1. A new JWT is generated.
     * 2. Any existing valid tokens for this user are revoked (Single Session Policy).
     * 3. The new token is persisted to the database for whitelist validation.
     * </p>
     *
     * @param loginRequest DTO containing the user's email and raw password.
     * @return {@link AuthResponse} containing the JWT and user details.
     * @throws BadCredentialsException If the email/password combination is incorrect.
     * @throws ResourceNotFoundException If the user exists in Auth context but not in the DB (Data inconsistency).
     */
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getEmail());

        try {
            // 1. Delegate authentication to the AuthenticationManager
            // This will use the UserDetailsService to verify the password hash.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 2. Update the SecurityContext for the current thread
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Retrieve the full User Entity from DB
            // We need the entity to associate it with the Token record in the database.
            Employee user = employeeRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User entity not found for email: " + loginRequest.getEmail()));

            // 4. Generate the JWT Token (Signed with Secret Key)
            String jwt = jwtUtils.generateJwtToken(authentication);

            // 5. Token Management (Security Policy)
            // Revoke all previous tokens to ensure only one active session exists (Optional but recommended).
            revokeAllUserTokens(user);
            // Persist the new token to the whitelist.
            saveUserToken(user, jwt);

            // 6. Extract Authority (Role) for the response payload
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            log.info("Login successful. Token issued for user: {}", loginRequest.getEmail());
            return new AuthResponse(jwt, userDetails.getUsername(), role);

        } catch (BadCredentialsException e) {
            log.warn("Login failed: Invalid credentials provided for {}", loginRequest.getEmail());
            throw e; // Rethrow to be handled by GlobalExceptionHandler
        } catch (Exception e) {
            log.error("Unexpected login error for user {}: {}", loginRequest.getEmail(), e.getMessage());
            throw e;
        }
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    /**
     * Persists the generated JWT to the database.
     * <p>
     * The token is saved with {@code revoked=false} and {@code expired=false}.
     * </p>
     *
     * @param user     The employee owner of the token.
     * @param jwtToken The JWT string.
     */
    private void saveUserToken(Employee user, String jwtToken) {
        var token = Token.builder()
                .employee(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    /**
     * Invalidates all valid tokens for the given user.
     * <p>
     * This method fetches all tokens where {@code expired=false} or {@code revoked=false}
     * and marks them as invalid. This prevents replay attacks using old tokens.
     * </p>
     *
     * @param user The employee whose tokens should be revoked.
     */
    private void revokeAllUserTokens(Employee user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;

        // Batch update status to Expired and Revoked
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}