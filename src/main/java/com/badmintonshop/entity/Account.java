package com.badmintonshop.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.badmintonshop.entity.enums.RoleName;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an employee within the Badminton Shop system.
 * <p>
 * Employees can have roles that define their permissions and access levels.
 * </p>
 */
@Entity
@Table(name = "accounts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Account implements UserDetails{
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
     * Indicates whether the employee's account is enabled.
     * <p>
     * If false, the employee cannot log in.
     * </p>
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * The role assigned to the employee.
     * <p>
     * Defines the set of permissions the employee has.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleName role;

    /**
     * Timestamps for auditing purposes.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; 
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
