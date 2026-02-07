package com.badmintonshop.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Use Spring's Transactional for readOnly support

import com.badmintonshop.entity.Account;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.payload.request.EmployeeRequest;
import com.badmintonshop.payload.response.EmployeeResponse;
import com.badmintonshop.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for managing Employee life-cycle operations.
 * <p>
 * This service handles creation, retrieval, updates, and deactivation (soft delete)
 * of employee accounts. It interacts with {@link EmployeeRepository} and enforces
 * business rules such as email uniqueness and password encryption.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves a list of all employees in the system.
     * <p>
     * Note: In a high-volume environment, pagination should be applied here.
     * </p>
     *
     * @return List of {@link EmployeeResponse} DTOs.
     */
    @Transactional(readOnly = true) // Performance optimization: Hibernate avoids dirty checking
    public List<EmployeeResponse> getAllEmployees() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves specific employee details by ID.
     *
     * @param id The unique identifier of the employee.
     * @return The employee details DTO.
     * @throws ResourceNotFoundException if no employee is found with the given ID.
     */
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return mapToResponse(account);
    }

    /**
     * Creates and persists a new employee account.
     * <p>
     * Logic:
     * 1. Checks if the email is already in use.
     * 2. Encrypts the raw password using BCrypt.
     * 3. Sets default security flags (account non-locked, etc.).
     * </p>
     *
     * @param request The data transfer object containing new employee details.
     * @return The created employee DTO.
     * @throws IllegalStateException if the email already exists.
     * @throws ResourceNotFoundException if the specified role is invalid.
     */
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // 1. Enforce unique email constraint
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email " + request.getEmail() + " is already taken.");
        }

        // 3. Build Entity with security defaults
        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    /**
     * Updates an existing employee's information.
     * <p>
     * Note: This method does not support password updates (handled separately).
     * The Role is updated only if a new value is provided and differs from the current one.
     * </p>
     *
     * @param id      The ID of the employee to update.
     * @param request The updated data.
     * @return The updated employee DTO.
     * @throws ResourceNotFoundException if the employee or role is not found.
     */
    // @Transactional
    // public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
    //     Account existingAccount = accountRepository.findById(id)
    //             .orElseThrow(() -> new ResourceNotFoundException("Not found Account with id: " + id));

    //     // Update basic fields
    //     existingAccount.setEmail(request.getEmail());

    //     return mapToResponse(accountRepository.save(existingAccount));
    // }

    /**
     * Performs a "Soft Delete" on an employee account.
     * <p>
     * Instead of removing the record from the database (which breaks referential integrity),
     * this method sets the {@code enabled} flag to {@code false}.
     * The employee will no longer be able to log in.
     * </p>
     *
     * @param id The ID of the employee to deactivate.
     * @throws ResourceNotFoundException if the employee is not found.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Account existing = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Account with id: " + id));

        // Soft delete: Deactivate the account
        existing.setEnabled(false);
        accountRepository.save(existing);
    }

    /**
     * Helper method to map Employee Entity to Response DTO.
     * <p>
     * Converts the entity to a DTO to prevent exposing internal details
     * (like encrypted passwords) to the client.
     * </p>
     *
     * @param employee The source entity.
     * @return The target DTO.
     */
    private EmployeeResponse mapToResponse(Account account) {
        return EmployeeResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .enabled(account.isEnabled())
                .build();
    }
}