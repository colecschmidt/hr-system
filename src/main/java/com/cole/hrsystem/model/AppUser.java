package com.cole.hrsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a system user (someone who can log in).
 * Separate from Employee — a manager is both an AppUser and an Employee.
 * This mirrors the separation you did in OnyxChat between auth users and message senders.
 */
@Entity
@Table(name = "app_users")
@Getter @Setter @NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum Role {
        ROLE_ADMIN,     // full access
        ROLE_MANAGER,   // can view/edit employees in their dept
        ROLE_EMPLOYEE   // read-only access to own record
    }

    public AppUser(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }
}
