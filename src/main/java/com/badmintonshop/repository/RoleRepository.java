package com.badmintonshop.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badmintonshop.entity.Role;
import com.badmintonshop.entity.enums.RoleName;

/**
 * Repository for managing Role entities.
 * Roles are static reference data, so caching is applied to reduce DB hits during authentication.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its enum name.
     * <p>
     * Usage: This method is called frequently during the user login/registration process.
     * Result is cached to improve performance.
     *
     * @param name The RoleName enum (e.g., ROLE_USER, ROLE_ADMIN).
     * @return An Optional containing the Role if found.
     */
    @Cacheable(value = "roles", key = "#name")
    Optional<Role> findByName(RoleName name);

    /**
     * Checks if a role exists by its name.
     * Useful for Data Seeding (creating default roles at application startup).
     *
     * @param name The RoleName enum.
     * @return true if the role exists, false otherwise.
     */
    boolean existsByName(RoleName name);
}