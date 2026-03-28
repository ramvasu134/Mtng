package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Teacher entity – admin accounts that can log into the Mtng dashboard.
 *
 * <p>DB design notes:</p>
 * <ul>
 *   <li>Surrogate PK (auto-increment Long) – avoids using mutable username as PK.</li>
 *   <li>Index on {@code username} for fast authentication lookup.</li>
 *   <li>Lifecycle timestamps for auditing and session management.</li>
 * </ul>
 */
@Entity
@Table(
    name = "teachers",
    indexes = {
        @Index(name = "idx_teacher_username", columnList = "username")
    }
)
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "ROLE_ADMIN";

    @Column(nullable = false)
    private String displayName;

    /** When this account was first created. */
    private LocalDateTime createdAt;

    /** When this account was last modified (password change, role update, etc.). */
    private LocalDateTime updatedAt;

    /** Last successful login – useful for detecting dormant accounts. */
    private LocalDateTime lastLogin;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getUsername()                { return username; }
    public void setUsername(String v)          { this.username = v; }

    public String getPassword()                { return password; }
    public void setPassword(String v)          { this.password = v; }

    public String getRole()                    { return role; }
    public void setRole(String v)              { this.role = v; }

    public String getDisplayName()             { return displayName; }
    public void setDisplayName(String v)       { this.displayName = v; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)    { this.updatedAt = v; }

    public LocalDateTime getLastLogin()          { return lastLogin; }
    public void setLastLogin(LocalDateTime v)    { this.lastLogin = v; }
}
