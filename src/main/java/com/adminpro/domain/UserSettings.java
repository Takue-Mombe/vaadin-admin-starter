package com.adminpro.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Per-application settings (single row, keyed by a well-known id=1).
 * Extend this entity with additional preference columns as the project grows.
 */
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    private Long id = 1L;

    @NotBlank(message = "Display name is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String displayName = "Admin User";

    @Size(max = 120)
    @Column(length = 120)
    private String email = "admin@adminpro.io";

    @Size(max = 30)
    @Column(length = 30)
    private String phone = "";

    @Column(nullable = false, length = 60)
    private String timezone = "UTC";

    @Column(nullable = false)
    private boolean emailNotifications = true;

    @Column(nullable = false)
    private boolean darkMode = false;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }
}
