package com.badmintonshop.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents an employee within the Badminton Shop system.
 * <p>
 * Employees can have roles that define their permissions and access levels.
 * </p>
 */
@Entity
@Table(name = "employees")
@Data
public class Employee implements UserDetails{
    /**
     * The unique identifier for the employee.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique email address of the employee.
     * <p>
     * Used for login and communication.
     * </p>
     */
    @Column(unique = true,nullable = false)
    private String email;

    /**
     * The hashed password for the employee's account.
     * <p>
     * Stored securely using hashing algorithms.
     * </p>
     */
    @Column(nullable = false)
    private String password;

    /**
     * The full name of the employee.
     * <p>
     * Used for display purposes in the system.
     * </p>
     */
    private String fullName;

    /**
     * The unique code assigned to the employee.
     * <p>
     * Used for internal identification and tracking.
     * </p>
     */
    @Column(unique = true)
    private String employeeCode;

    /**
     * Indicates whether the employee's account is enabled.
     * <p>
     * If false, the employee cannot log in.
     * </p>
     */
    private boolean enabled = true;

    /**
     * Indicates whether the employee's account is locked.
     * <p>
     * If false, the employee cannot log in.
     * </p>
     */
    private boolean accountNonLocked = true;

    /**
     * Indicates whether the employee's account has expried.
     * <p>
     * If false, the employee cannot log in.
     * </p>
     */
    private boolean accountNonExpired = true;
    
    /**
     * Indicates whether the employee's credentials have expired.
     * <p>
     * If false, the employee cannot log in.
     * </p>
     */
    private boolean credentialsNonExpired = true;

    /**
     * The role assigned to the employee.
     * <p>
     * Defines the set of permissions the employee has.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    /**
     * Timestamps for auditing purposes.
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getName()));
    }

    @Override
    public String getUsername() {
        // TODO Auto-generated method stub
        return this.email;
    }
}
