package com.badmintonshop.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.badmintonshop.payload.request.EmployeeRequest;
import com.badmintonshop.payload.response.EmployeeResponse;
import com.badmintonshop.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing Employee resources.
 * <p>
 * This controller provides administrative endpoints for CRUD operations on employees.
 * Access is strictly restricted to users with the 'ADMIN' role via {@link PreAuthorize}.
 * </p>
 *
 * @see EmployeeService
 */
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    /**
     * Retrieves a list of all registered employees.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/admin/employees
     * </p>
     *
     * @return A ResponseEntity containing the list of {@link EmployeeResponse} DTOs.
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * Retrieves detailed information about a specific employee by their ID.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/admin/employees/{id}
     * </p>
     *
     * @param id The unique identifier of the employee.
     * @return A ResponseEntity containing the {@link EmployeeResponse}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /**
     * Registers a new employee in the system.
     * <p>
     * The request body is validated against {@link EmployeeRequest} constraints.
     * Upon success, returns HTTP 201 (Created).
     * HTTP Method: POST
     * Endpoint: /api/admin/employees
     * </p>
     *
     * @param request The payload containing the new employee's details.
     * @return A ResponseEntity containing the created employee data and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody @Valid EmployeeRequest request) {
        // IMPORTANT: Added @Valid to trigger validation annotations in the DTO
        // Changed return status from 200 OK to 201 CREATED for standard REST practice
        return new ResponseEntity<>(employeeService.createEmployee(request), HttpStatus.CREATED);
    }

    /**
     * Updates an existing employee's information.
     * <p>
     * HTTP Method: PUT
     * Endpoint: /api/admin/employees/{id}
     * </p>
     *
     * @param id      The unique identifier of the employee to update.
     * @param request The payload containing updated details.
     * @return A ResponseEntity containing the updated employee data.
     */
    // @PutMapping("/{id}")
    // public ResponseEntity<EmployeeResponse> updateEmployee(
    //         @PathVariable Long id,
    //         @RequestBody @Valid EmployeeRequest request
    // ) {
    //     // Added @Valid here as well
    //     return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    // }

    /**
     * Deactivates (Soft Deletes) an employee account.
     * <p>
     * The employee record is not physically removed from the database but is marked as disabled.
     * HTTP Method: DELETE
     * Endpoint: /api/admin/employees/{id}
     * </p>
     *
     * @param id The unique identifier of the employee to deactivate.
     * @return A ResponseEntity with a success message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deactivated successfully");
    }
}