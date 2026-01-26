package com.badmintonshop.repository;

import com.badmintonshop.entity.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Employee} entities.
 * <p>
 * This repository is a critical component of the Authentication and Authorization module,
 * handling user retrieval for login processing and uniqueness validation during registration.
 * </p>
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Retrieves an employee by their email address.
     * <p>
     * This is the primary method used by Spring Security's {@code UserDetailsService}
     * to load user data during the authentication process.
     * </p>
     * <p>
     * <b>Performance Note:</b> The {@code @EntityGraph} annotation is used here to EAGERly fetch
     * the associated {@code Role} in a single SQL query, avoiding the N+1 select problem.
     * </p>
     *
     * @param email The email address to search for.
     * @return An {@link Optional} containing the employee if found, or empty otherwise.
     */
    @EntityGraph(attributePaths = "role")
    Optional<Employee> findByEmail(String email);

    /**
     * Checks if an email address is already registered in the system.
     * <p>
     * Used primarily for form validation during the new employee registration process
     * to prevent Unique Constraint violations at the database level.
     * </p>
     *
     * @param email The email to check.
     * @return {@code true} if the email exists, {@code false} otherwise.
     */
    Boolean existsByEmail(String email);

    /**
     * Checks if an employee code (e.g., "NV001") is already in use.
     * <p>
     * Ensures that business-specific identifiers remain unique across the organization.
     * </p>
     *
     * @param employeeCode The code to check.
     * @return {@code true} if the code exists, {@code false} otherwise.
     */
    Boolean existsByEmployeeCode(String employeeCode);
}