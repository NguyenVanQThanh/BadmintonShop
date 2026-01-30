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
 * DataSeeder initializes the database with critical reference data.
 * <p>
 * This class ensures that Roles, Permissions, and default Admin/Staff accounts
 * exist in the database upon application startup.
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

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data seeding process...");
        long startTime = System.currentTimeMillis();

        // 1. Initialize all permissions defined in the Enum
        Map<PermissionName, Permission> permissions = initPermissions();

        // 2. Initialize roles and assign specific permissions
        Map<RoleName, Role> roles = initRoles(permissions);

        // 3. Initialize default employees
        initEmployees(roles);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Data seeding completed successfully in {} ms.", duration);
    }

    // =========================================================================
    // 1. PERMISSION INITIALIZATION
    // =========================================================================

    private Map<PermissionName, Permission> initPermissions() {
        Map<PermissionName, Permission> savedPermissions = new HashMap<>();
        
        // Iterate through all Enum constants to ensure DB is in sync with Code
        for (PermissionName permissionName : PermissionName.values()) {
            Permission permission = permissionRepository.findByName(permissionName)
                .orElseGet(() -> {
                    Permission newPerm = new Permission();
                    newPerm.setName(permissionName);
                    newPerm.setDescription("System generated permission for " + permissionName.name());
                    log.info("Created new Permission: {}", permissionName.name());
                    return permissionRepository.save(newPerm);
                });
            savedPermissions.put(permissionName, permission);
        }
        return savedPermissions;
    }

    // =========================================================================
    // 2. ROLE INITIALIZATION
    // =========================================================================

    private Map<RoleName, Role> initRoles(Map<PermissionName, Permission> allPerms) {
        Map<RoleName, Role> savedRoles = new HashMap<>();

        // --- ADMIN: Full Access ---
        Set<Permission> adminPerms = new HashSet<>(allPerms.values());
        savedRoles.put(RoleName.ADMIN, createRole(RoleName.ADMIN, adminPerms));

        // --- MANAGER: High level access, restricted from critical deletions ---
        Set<Permission> managerPerms = allPerms.entrySet().stream()
                .filter(entry -> !entry.getKey().name().endsWith("_DELETE")) // Cannot delete anything
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        savedRoles.put(RoleName.MANAGER, createRole(RoleName.MANAGER, managerPerms));

        // --- STAFF: Sales focused ---
        // Access: Order (CRUD without Delete), Customer (CRUD), Product (Read), Inventory (Read)
        Set<PermissionName> staffAllowed = Set.of(
            PermissionName.ORDER_READ, PermissionName.ORDER_CREATE, PermissionName.ORDER_UPDATE,
            PermissionName.CUSTOMER_READ, PermissionName.CUSTOMER_CREATE, PermissionName.CUSTOMER_UPDATE,
            PermissionName.PRODUCT_READ,
            PermissionName.INVENTORY_READ,
            PermissionName.DASHBOARD_VIEW
        );
        
        Set<Permission> staffPerms = allPerms.entrySet().stream()
                .filter(entry -> staffAllowed.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        savedRoles.put(RoleName.STAFF, createRole(RoleName.STAFF, staffPerms));

        // --- WAREHOUSE: Logistics focused ---
        // Access: Inventory (Read/Import/Export), Product (Read), Order (Read/Update status)
        Set<PermissionName> warehouseAllowed = Set.of(
            PermissionName.INVENTORY_READ, PermissionName.INVENTORY_IMPORT, PermissionName.INVENTORY_EXPORT,
            PermissionName.PRODUCT_READ,
            PermissionName.ORDER_READ, PermissionName.ORDER_UPDATE
        );

        Set<Permission> warehousePerms = allPerms.entrySet().stream()
                .filter(entry -> warehouseAllowed.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        savedRoles.put(RoleName.WAREHOUSE, createRole(RoleName.WAREHOUSE, warehousePerms));

        return savedRoles;
    }

    private Role createRole(RoleName name, Set<Permission> permissions) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(name);
            newRole.setPermissions(permissions);
            log.info("Created new Role: {} with {} permissions", name, permissions.size());
            return roleRepository.save(newRole);
        });
    }

    // =========================================================================
    // 3. EMPLOYEE INITIALIZATION
    // =========================================================================

    private void initEmployees(Map<RoleName, Role> roles) {
        createEmployee("admin@gmail.com", "Super Admin", roles.get(RoleName.ADMIN));
        createEmployee("manager@gmail.com", "Store Manager", roles.get(RoleName.MANAGER));
        createEmployee("staff@gmail.com", "Sales Staff", roles.get(RoleName.STAFF));
        createEmployee("warehouse@gmail.com", "Stock Keeper", roles.get(RoleName.WAREHOUSE));
    }

    private void createEmployee(String email, String fullName, Role role) {
        if (!employeeRepository.existsByEmail(email)) {
            Employee employee = new Employee();
            employee.setEmail(email);
            employee.setFullName(fullName);
            employee.setPassword(passwordEncoder.encode("123456")); // Default password for seeding
            employee.setRole(role);
            employee.setEnabled(true);
            
            employeeRepository.save(employee); 
            log.info("Created Employee account: {} with Role: {}", email, role.getName());
        } else {
            log.debug("Employee account {} already exists. Skipping.", email);
        }
    }
}