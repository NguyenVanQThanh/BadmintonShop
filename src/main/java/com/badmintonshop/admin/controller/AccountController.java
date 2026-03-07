package com.badmintonshop.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.badmintonshop.admin.dto.AccountRequest;
import com.badmintonshop.admin.payload.response.AccountResponse;
import com.badmintonshop.admin.service.AccountService;
import com.badmintonshop.shared.entity.Account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing Account resources.
 * <p>
 * This controller provides endpoints for both user account operations and administrative account management.
 * Access is controlled at the method level via {@link PreAuthorize} annotations:
 * - User endpoints (e.g., /me) require ADMIN or CASHIER roles
 * - Admin endpoints (e.g., /api/accounts) require ADMIN role only
 * </p>
 *
 * @see AccountService
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Retrieves the currently logged-in user's account information.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/accounts/me
     * </p>
     *
     * @param loggedInUser The authenticated user from the security context.
     * @return A ResponseEntity containing the current user's account details.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<AccountResponse> getCurrentUser(@AuthenticationPrincipal Account loggedInUser) {
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(accountService.getAccountById(loggedInUser.getId()));
    }

    /**
     * Retrieves a list of all registered accounts.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/accounts
     * </p>
     *
     * @return A ResponseEntity containing the list of {@link AccountResponse} DTOs.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    /**
     * Retrieves detailed information about a specific account by their ID.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/accounts/{id}
     * </p>
     *
     * @param id The unique identifier of the account.
     * @return A ResponseEntity containing the {@link AccountResponse}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    /**
     * Registers a new account in the system.
     * <p>
     * The request body is validated against {@link AccountRequest} constraints.
     * Upon success, returns HTTP 201 (Created).
     * HTTP Method: POST
     * Endpoint: /api/accounts
     * </p>
     *
     * @param request The payload containing the new account's details.
     * @return A ResponseEntity containing the created account data and HTTP status 201.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid AccountRequest request) {
        return new ResponseEntity<>(accountService.createAccount(request), HttpStatus.CREATED);
    }

    /**
     * Deactivates (Soft Deletes) an account.
     * <p>
     * The account record is not physically removed from the database but is marked as disabled.
     * HTTP Method: DELETE
     * Endpoint: /api/accounts/{id}
     * </p>
     *
     * @param id The unique identifier of the account to deactivate.
     * @return A ResponseEntity with a success message.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok("Account deactivated successfully");
    }
}
