package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Student entity – participants managed by the teacher in Mtng */
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;   // BCrypt-encoded, used by Spring Security

    /** Plain-text password kept only for admin display & WhatsApp sharing */
    @Column(name = "raw_password")
    private String rawPassword;

    private boolean deviceLock      = false;
    private boolean showRecordings  = true;
    private boolean blocked         = false;
    private boolean muted           = false;

    /** ONLINE | OFFLINE */
    private String status = "OFFLINE";

    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastSeen  = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }

    public String getUsername()                  { return username; }
    public void setUsername(String username)     { this.username = username; }

    public String getPassword()                  { return password; }
    public void setPassword(String password)     { this.password = password; }

    public String getRawPassword()               { return rawPassword; }
    public void setRawPassword(String v)         { this.rawPassword = v; }

    public boolean isDeviceLock()                { return deviceLock; }
    public void setDeviceLock(boolean v)         { this.deviceLock = v; }

    public boolean isShowRecordings()            { return showRecordings; }
    public void setShowRecordings(boolean v)     { this.showRecordings = v; }

    public boolean isBlocked()                   { return blocked; }
    public void setBlocked(boolean v)            { this.blocked = v; }

    public boolean isMuted()                     { return muted; }
    public void setMuted(boolean v)              { this.muted = v; }

    public String getStatus()                    { return status; }
    public void setStatus(String status)         { this.status = status; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }

    public LocalDateTime getLastSeen()           { return lastSeen; }
    public void setLastSeen(LocalDateTime v)     { this.lastSeen = v; }
}

