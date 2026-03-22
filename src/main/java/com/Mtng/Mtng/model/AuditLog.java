package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AuditLog entity – tracks important actions in the system for auditing.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who performed the action (username) */
    @Column(nullable = false)
    private String actor;

    /** Action type: LOGIN, LOGOUT, MEETING_START, MEETING_STOP, JOIN, LEAVE, CHAT, etc. */
    @Column(nullable = false)
    private String action;

    /** Human-readable description of what happened */
    @Column(length = 500)
    private String details;

    /** IP address of the client */
    private String ipAddress;

    /** Timestamp of the event */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getActor()                     { return actor; }
    public void setActor(String actor)           { this.actor = actor; }

    public String getAction()                    { return action; }
    public void setAction(String action)         { this.action = action; }

    public String getDetails()                   { return details; }
    public void setDetails(String details)       { this.details = details; }

    public String getIpAddress()                 { return ipAddress; }
    public void setIpAddress(String ipAddress)   { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp()           { return timestamp; }
    public void setTimestamp(LocalDateTime v)     { this.timestamp = v; }
}

