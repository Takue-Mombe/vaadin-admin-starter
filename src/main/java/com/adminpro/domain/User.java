package com.adminpro.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Core User entity stored in H2 via JPA.
 * Bean Validation annotations are reused by Vaadin Binder automatically.
 */
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String passwordHash = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.VIEWER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(String firstName, String lastName, String email, Role role, boolean active) {
        this(firstName, lastName, email, role, active, "");
    }

    public User(String firstName,
                String lastName,
                String email,
                Role role,
                boolean active,
                String passwordHash) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.role      = role;
        this.active    = active;
        this.passwordHash = passwordHash;
    }

    // ── Convenience ──────────────────────────────────────────────────────────

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
