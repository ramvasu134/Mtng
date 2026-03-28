package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AuditLog entity – tracks important actions in the system for auditing.
 *
 * <p>DB design notes:</p>
 * <ul>
 *   <li>Composite index on (actor, timestamp) covers findByActorOrderByTimestampDesc –
 *       the most common query (per-user history).</li>
 *   <li>Composite index on (action, timestamp) covers findByActionOrderByTimestampDesc –
 *       per-action type analysis.</li>
 *   <li>Single-column index on timestamp for range queries (date-range audit reports).</li>
 *   <li>For large deployments: consider range-partitioning this table by month on timestamp,
 *       and archiving rows older than 90 days to an audit_logs_archive table.</li>
 * </ul>
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_actor_ts",  columnList = "actor, timestamp"),
        @Index(name = "idx_audit_action_ts", columnList = "action, timestamp"),
        @Index(name = "idx_audit_ts",        columnList = "timestamp")
    }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who performed the action (username). */
    @Column(nullable = false, length = 100)
    private String actor;

    /** Action type: LOGIN, LOGOUT, MEETING_START, MEETING_STOP, JOIN, LEAVE, CHAT, etc.
     *  VARCHAR(30) is sufficient – avoids over-allocating VARCHAR(255). */
    @Column(nullable = false, length = 30)
    private String action;

    /** Human-readable description. */
    @Column(length = 500)
    private String details;

    /** IP address of the client (IPv6 max is 45 chars). */
    @Column(length = 45)
    private String ipAddress;

    /** Timestamp of the event – indexed for range queries and composite lookups. */
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

    public LocalDateTime getTimestamp()          { return timestamp; }
    public void setTimestamp(LocalDateTime v)    { this.timestamp = v; }
}
