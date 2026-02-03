package com.badmintonshop.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Use Spring's Transactional for readOnly support

import com.badmintonshop.entity.Employee;
import com.badmintonshop.entity.Role;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.payload.request.EmployeeRequest;
import com.badmintonshop.payload.response.EmployeeResponse;
import com.badmintonshop.repository.EmployeeRepository;
import com.badmintonshop.repository.RoleRepository;

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

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

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
        return employeeRepository.findAll().stream()
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
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToResponse(employee);
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
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email " + request.getEmail() + " is already taken.");
        }

        // 2. Fetch and validate Role
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role configuration not found for: " + request.getRole()));

        // 3. Build Entity with security defaults
        Employee employee = Employee.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // CRITICAL: Always encode passwords
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .employeeCode(request.getEmployeeCode())
                .role(role)
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();

        Employee saved = employeeRepository.save(employee);
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
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Update basic fields
        existingEmployee.setFullName(request.getFullName());
        existingEmployee.setPhoneNumber(request.getPhoneNumber());

        // Update Role logic: Only if provided and different
        if (request.getRole() != null && !existingEmployee.getRole().getName().equals(request.getRole())) {
            Role newRole = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Role configuration not found for: " + request.getRole()));
            existingEmployee.setRole(newRole);
        }

        return mapToResponse(employeeRepository.save(existingEmployee));
    }

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
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Employee with id: " + id));

        // Soft delete: Deactivate the account
        existing.setEnabled(false);
        employeeRepository.save(existing);
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
    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .phoneNumber(employee.getPhoneNumber())
                .employeeCode(employee.getEmployeeCode())
                // Safe handling of Role to avoid NullPointerException
                .role(employee.getRole() != null ? employee.getRole().getName().name() : "N/A")
                .enabled(employee.isEnabled())
                .build();
    }
}