package com.badmintonshop.config;

import com.badmintonshop.entity.Employee;
import com.badmintonshop.entity.Permission;
import com.badmintonshop.entity.Role;
import com.badmintonshop.entity.enums.PermissionName;
import com.badmintonshop.entity.enums.RoleName;
import com.badmintonshop.repository.EmployeeRepository;
import com.badmintonshop.repository.PermissionRepository;
import com.badmintonshop.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DataSeeder initializes the database with critical reference data upon application startup.
 * <p>
 * This component is designed to be idempotent. It checks for the existence of
 * entities (Permissions, Roles, Employees) before attempting to create them.
 * This ensures compatibility with the 'update' DDL generation strategy, preventing
 * duplicate data entry errors during server restarts.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Entry point for the seeding process.
     * <p>
     * Executed automatically after the Spring Application Context is loaded.
     * Transactional context ensures data integrity during the seeding batch.
     * </p>
     *
     * @param args Command line arguments (not used).
     */
    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding process...");
        long startTime = System.currentTimeMillis();

        // 1. Initialize Permissions
        Map<PermissionName, Permission> permissions = initPermissions();

        // 2. Initialize Roles with mapped Permissions
        Map<RoleName, Role> roles = initRoles(permissions);

        // 3. Initialize Default Employees
        initEmployees(roles);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Data seeding completed successfully in {} ms.", duration);
    }

    // =========================================================================
    // 1. PERMISSION INITIALIZATION
    // =========================================================================

    /**
     * Ensures all permissions defined in the {@link PermissionName} enum exist in the database.
     *
     * @return A map of PermissionName to the persisted Permission entity.
     */
    private Map<PermissionName, Permission> initPermissions() {
        Map<PermissionName, Permission> savedPermissions = new HashMap<>();

        for (PermissionName permissionName : PermissionName.values()) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseGet(() -> {
                        Permission newPerm = new Permission();
                        newPerm.setName(permissionName);
                        newPerm.setDescription("System generated permission for " + permissionName.name());
                        log.info("Seeding new Permission: {}", permissionName.name());
                        return permissionRepository.save(newPerm);
                    });
            savedPermissions.put(permissionName, permission);
        }
        return savedPermissions;
    }

    // =========================================================================
    // 2. ROLE INITIALIZATION
    // =========================================================================

    /**
     * Ensures all standard roles exist in the database and assigns default permissions.
     *
     * @param allPerms A map of all available permissions.
     * @return A map of RoleName to the persisted Role entity.
     */
    private Map<RoleName, Role> initRoles(Map<PermissionName, Permission> allPerms) {
        Map<RoleName, Role> savedRoles = new HashMap<>();

        // --- ADMIN Configuration ---
        Set<Permission> adminPerms = new HashSet<>(allPerms.values());
        savedRoles.put(RoleName.ADMIN, createRoleIfNotFound(RoleName.ADMIN, adminPerms));

        // --- MANAGER Configuration ---
        Set<Permission> managerPerms = allPerms.entrySet().stream()
                .filter(entry -> !entry.getKey().name().endsWith("_DELETE"))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        savedRoles.put(RoleName.MANAGER, createRoleIfNotFound(RoleName.MANAGER, managerPerms));

        // --- STAFF Configuration ---
        Set<PermissionName> staffAllowed = Set.of(
                PermissionName.ORDER_READ, PermissionName.ORDER_CREATE, PermissionName.ORDER_UPDATE,
                PermissionName.CUSTOMER_READ, PermissionName.CUSTOMER_CREATE, PermissionName.CUSTOMER_UPDATE,
                PermissionName.PRODUCT_READ,
                PermissionName.INVENTORY_READ,
                PermissionName.DASHBOARD_VIEW
        );
        Set<Permission> staffPerms = filterPermissions(allPerms, staffAllowed);
        savedRoles.put(RoleName.STAFF, createRoleIfNotFound(RoleName.STAFF, staffPerms));

        // --- WAREHOUSE Configuration ---
        Set<PermissionName> warehouseAllowed = Set.of(
                PermissionName.INVENTORY_READ, PermissionName.INVENTORY_IMPORT, PermissionName.INVENTORY_EXPORT,
                PermissionName.PRODUCT_READ,
                PermissionName.ORDER_READ, PermissionName.ORDER_UPDATE
        );
        Set<Permission> warehousePerms = filterPermissions(allPerms, warehouseAllowed);
        savedRoles.put(RoleName.WAREHOUSE, createRoleIfNotFound(RoleName.WAREHOUSE, warehousePerms));

        return savedRoles;
    }

    private Role createRoleIfNotFound(RoleName name, Set<Permission> permissions) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(name);
                    newRole.setPermissions(permissions);
                    log.info("Seeding new Role: {}", name);
                    return roleRepository.save(newRole);
                });
    }

    private Set<Permission> filterPermissions(Map<PermissionName, Permission> allPerms, Set<PermissionName> allowedNames) {
        return allPerms.entrySet().stream()
                .filter(entry -> allowedNames.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    // =========================================================================
    // 3. EMPLOYEE INITIALIZATION
    // =========================================================================

    /**
     * Ensures default accounts exist for each role.
     *
     * @param roles A map of available roles.
     */
    private void initEmployees(Map<RoleName, Role> roles) {
        // Param order: Email, Full Name, Role, Phone, Employee Code
        createEmployeeIfNotFound("admin@gmail.com", "Super Admin", roles.get(RoleName.ADMIN), "0900000001", "ADM001");
        createEmployeeIfNotFound("manager@gmail.com", "Store Manager", roles.get(RoleName.MANAGER), "0900000002", "MGR001");
        createEmployeeIfNotFound("staff@gmail.com", "Sales Staff", roles.get(RoleName.STAFF), "0900000003", "STF001");
        createEmployeeIfNotFound("warehouse@gmail.com", "Stock Keeper", roles.get(RoleName.WAREHOUSE), "0900000004", "WAR001");
    }

    private void createEmployeeIfNotFound(String email, String fullName, Role role, String phoneNumber, String empCode) {
        if (employeeRepository.existsByEmail(email)) {
            log.debug("Employee account {} already exists. Skipping.", email);
            return;
        }

        Employee employee = Employee.builder()
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode("123456")) // Default password
                .role(role)
                .phoneNumber(phoneNumber)
                .employeeCode(empCode)
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();

        employeeRepository.save(employee);
        log.info("Seeding new Employee account: {} ({})", email, role.getName());
    }
}