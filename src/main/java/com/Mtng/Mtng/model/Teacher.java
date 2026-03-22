package com.Mtng.Mtng.model;

import jakarta.persistence.*;

/**
 * Teacher entity – admin accounts that can log into the Mtng dashboard.
 */
@Entity
@Table(name = "teachers")
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
}

