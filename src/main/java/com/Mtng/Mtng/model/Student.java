package com.Mtng.Mtng.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Student entity – participants managed by the teacher in Mtng.
 *
 * <p>DB design notes:</p>
 * <ul>
 *   <li>Surrogate PK (auto-increment Long).</li>
 *   <li>@Version provides optimistic locking to avoid lost-updates on concurrent status changes.</li>
 *   <li>Indexes on {@code status} (WHERE status = 'ONLINE') and {@code username} (login lookups).</li>
 *   <li>Proper data types: boolean flags use BIT(1), timestamps use TIMESTAMP.</li>
 * </ul>
 */
@Entity
@Table(
    name = "students",
    indexes = {
        @Index(name = "idx_student_username", columnList = "username"),
        @Index(name = "idx_student_status",   columnList = "status")
    }
)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic-locking version – prevents concurrent save anomalies. */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;   // BCrypt-encoded, used by Spring Security

    /** Plain-text password kept only for admin display & WhatsApp sharing. */
    @JsonIgnore
    @Column(name = "raw_password")
    private String rawPassword;

    private String email;
    private String phone;

    private boolean deviceLock     = false;
    private boolean showRecordings = true;
    private boolean blocked        = false;
    private boolean muted          = false;

    /** ONLINE | OFFLINE – VARCHAR(10) is sufficient, avoids large heap overhead. */
    @Column(length = 10)
    private String status = "OFFLINE";

    private LocalDateTime createdAt;

    /** Updated whenever any field changes – aids incremental sync / auditing. */
    private LocalDateTime updatedAt;

    private LocalDateTime lastSeen;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        lastSeen  = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public Long getVersion()                     { return version; }
    public void setVersion(Long version)         { this.version = version; }

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

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getPhone()                     { return phone; }
    public void setPhone(String phone)           { this.phone = phone; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)    { this.updatedAt = v; }

    public LocalDateTime getLastSeen()           { return lastSeen; }
    public void setLastSeen(LocalDateTime v)     { this.lastSeen = v; }
}
