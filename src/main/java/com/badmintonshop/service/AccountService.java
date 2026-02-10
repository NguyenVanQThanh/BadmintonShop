package com.badmintonshop.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Use Spring's Transactional for readOnly support

import com.badmintonshop.entity.Account;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.payload.request.AccountRequest;
import com.badmintonshop.payload.response.AccountResponse;
import com.badmintonshop.repository.AccountRepository;
import com.badmintonshop.entity.enums.RoleName;

import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for managing account life-cycle operations.
 * <p>
 * This service handles creation, retrieval, updates, and deactivation (soft delete)
 * of account accounts. It interacts with {@link accountRepository} and enforces
 * business rules such as email uniqueness and password encryption.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves a list of all accounts in the system.
     * <p>
     * Note: In a high-volume environment, pagination should be applied here.
     * </p>
     *
     * @return List of {@link AccountResponse} DTOs.
     */
    @Transactional(readOnly = true) // Performance optimization: Hibernate avoids dirty checking
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves specific account details by ID.
     *
     * @param id The unique identifier of the account.
     * @return The account details DTO.
     * @throws ResourceNotFoundException if no account is found with the given ID.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return mapToResponse(account);
    }

    /**
     * Creates and persists a new account account.
     * <p>
     * Logic:
     * 1. Checks if the email is already in use.
     * 2. Encrypts the raw password using BCrypt.
     * 3. Sets default security flags (account non-locked, etc.).
     * </p>
     *
     * @param request The data transfer object containing new account details.
     * @return The created account DTO.
     * @throws IllegalStateException if the email already exists.
     * @throws ResourceNotFoundException if the specified role is invalid.
     */
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        // 1. Enforce unique email constraint
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email " + request.getEmail() + " is already taken.");
        }
        if (!RoleName.isValidRole(request.getRole())) {
            throw new ResourceNotFoundException("Role not found: " + request.getRole());
        }
        // 3. Build Entity with security defaults
        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    /**
     * Updates an existing account's information.
     * <p>
     * Note: This method does not support password updates (handled separately).
     * The Role is updated only if a new value is provided and differs from the current one.
     * </p>
     *
     * @param id      The ID of the account to update.
     * @param request The updated data.
     * @return The updated account DTO.
     * @throws ResourceNotFoundException if the account or role is not found.
     */
    // @Transactional
    // public AccountResponse updateaccount(Long id, accountRequest request) {
    //     Account existingAccount = accountRepository.findById(id)
    //             .orElseThrow(() -> new ResourceNotFoundException("Not found Account with id: " + id));

    //     // Update basic fields
    //     existingAccount.setEmail(request.getEmail());

    //     return mapToResponse(accountRepository.save(existingAccount));
    // }

    /**
     * Performs a "Soft Delete" on an account account.
     * <p>
     * Instead of removing the record from the database (which breaks referential integrity),
     * this method sets the {@code enabled} flag to {@code false}.
     * The account will no longer be able to log in.
     * </p>
     *
     * @param id The ID of the account to deactivate.
     * @throws ResourceNotFoundException if the account is not found.
     */
    @Transactional
    public void deleteAccount(Long id) {
        Account existing = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Account with id: " + id));

        // Soft delete: Deactivate the account
        existing.setEnabled(false);
        accountRepository.save(existing);
    }

    /**
     * Helper method to map account Entity to Response DTO.
     * <p>
     * Converts the entity to a DTO to prevent exposing internal details
     * (like encrypted passwords) to the client.
     * </p>
     *
     * @param account The source entity.
     * @return The target DTO.
     */
    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .enabled(account.isEnabled())
                .role(account.getRole().name())
                .build();
    }
}