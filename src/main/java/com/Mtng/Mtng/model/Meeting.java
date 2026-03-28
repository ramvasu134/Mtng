package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Meeting entity – represents a live session/room in Mtng (supports multiple concurrent rooms).
 *
 * <p>DB design notes (3NF / long-term stability):</p>
 * <ul>
 *   <li>Surrogate PK (auto-increment Long) – fast joins, stable FK target.</li>
 *   <li>invitedParticipants stored as a normalised join table (1NF – no comma lists).</li>
 *   <li>@Version column enables optimistic locking to prevent lost-update anomalies.</li>
 *   <li>@Table indexes on the most-queried columns: active, room_name, created_by.</li>
 * </ul>
 */
@Entity
@Table(
    name = "meetings",
    indexes = {
        @Index(name = "idx_meeting_active",      columnList = "active"),
        @Index(name = "idx_meeting_room_name",   columnList = "room_name"),
        @Index(name = "idx_meeting_created_by",  columnList = "created_by")
    }
)
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic-locking version – prevents lost-update on concurrent saves. */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private String title;

    /** Stable room name – all users in this room join the same signaling topic. */
    @Column(name = "room_name", unique = true)
    private String roomName;

    private boolean active        = false;
    private boolean fullRecording = false;

    private int inMeetingCount = 0;
    private int onlineCount    = 0;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * Normalised (1NF) set of invited participant usernames.
     * Stored in a separate join table: meeting_invited_participants(meeting_id, username).
     * Replaces the old comma-separated VARCHAR column to eliminate search inefficiency
     * and partial-dependency violations.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "meeting_invited_participants",
        joinColumns = @JoinColumn(name = "meeting_id"),
        indexes = @Index(name = "idx_mip_meeting_id", columnList = "meeting_id")
    )
    @Column(name = "username", nullable = false, length = 100)
    private Set<String> invitedParticipants = new HashSet<>();

    /** Username of the admin who created this room. */
    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    protected void onCreate() { if (title == null) title = "New Meeting"; }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }

    public Long getVersion()                         { return version; }
    public void setVersion(Long version)             { this.version = version; }

    public String getTitle()                         { return title; }
    public void setTitle(String title)               { this.title = title; }

    public String getRoomName()                      { return roomName; }
    public void setRoomName(String roomName)         { this.roomName = roomName; }

    public boolean isActive()                        { return active; }
    public void setActive(boolean active)            { this.active = active; }

    public boolean isFullRecording()                 { return fullRecording; }
    public void setFullRecording(boolean v)          { this.fullRecording = v; }

    public int getInMeetingCount()                   { return inMeetingCount; }
    public void setInMeetingCount(int v)             { this.inMeetingCount = v; }

    public int getOnlineCount()                      { return onlineCount; }
    public void setOnlineCount(int v)                { this.onlineCount = v; }

    public LocalDateTime getStartTime()              { return startTime; }
    public void setStartTime(LocalDateTime v)        { this.startTime = v; }

    public LocalDateTime getEndTime()                { return endTime; }
    public void setEndTime(LocalDateTime v)          { this.endTime = v; }

    /** Returns the normalised set of invited participant usernames. */
    public Set<String> getInvitedParticipants()              { return invitedParticipants; }
    public void setInvitedParticipants(Set<String> v)        { this.invitedParticipants = v != null ? v : new HashSet<>(); }

    public String getCreatedBy()                     { return createdBy; }
    public void setCreatedBy(String v)               { this.createdBy = v; }
}
