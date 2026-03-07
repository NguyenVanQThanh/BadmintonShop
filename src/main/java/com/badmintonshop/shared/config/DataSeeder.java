package com.badmintonshop.shared.config;

import com.badmintonshop.shared.entity.Account;
import com.badmintonshop.shared.entity.enums.RoleName;
import com.badmintonshop.shared.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    private final AccountRepository accountRepository;
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
        initEmployees();
        long duration = System.currentTimeMillis() - startTime;
        log.info("Data seeding completed successfully in {} ms.", duration);
    }

    // =========================================================================
    // 3. EMPLOYEE INITIALIZATION
    // =========================================================================

    /**
     * Ensures default accounts exist for each role.
     *
     * @param roles A map of available roles.
     */
    private void initEmployees() {
        // Param order: Email, Full Name, Role, Phone, Employee Code
        createEmployeeIfNotFound("admin@gmail.com", RoleName.ADMIN, "0900000001", "ADM001");
        createEmployeeIfNotFound("cashier1@gmail.com", RoleName.CASHIER, "0900000002", "MGR001");
        createEmployeeIfNotFound("cashier2@gmail.com",RoleName.CASHIER, "0900000003", "STF001");
        createEmployeeIfNotFound("cashier3@gmail.com",RoleName.CASHIER, "0900000004", "WAR001");
    }

    private void createEmployeeIfNotFound(String email, RoleName role, String phoneNumber, String empCode) {
        if (accountRepository.existsByEmail(email)) {
            log.debug("Employee account {} already exists. Skipping.", email);
            return;
        }

        Account account = Account.builder()
                .email(email)
                .password(passwordEncoder.encode("123456")) // Default password
                .role(role)
                .status(com.badmintonshop.shared.entity.enums.AccountStatus.ACTIVE)
                .build();

        accountRepository.save(account);
        log.info("Seeding new account: {} ({})", email, role.name());
    }
}