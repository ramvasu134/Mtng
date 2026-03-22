package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Meeting entity – represents a live session in Mtng */
@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    /** Stable Jitsi room name – all users must join the same room */
    @Column(unique = true)
    private String roomName;

    private boolean active = false;
    private boolean fullRecording = false;

    private int inMeetingCount  = 0;
    private int onlineCount     = 0;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @PrePersist
    protected void onCreate() { if (title == null) title = "New Meeting"; }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId()                       { return id; }
    public void setId(Long id)                { this.id = id; }

    public String getTitle()                  { return title; }
    public void setTitle(String title)        { this.title = title; }

    public String getRoomName()               { return roomName; }
    public void setRoomName(String roomName)  { this.roomName = roomName; }

    public boolean isActive()                 { return active; }
    public void setActive(boolean active)     { this.active = active; }

    public boolean isFullRecording()              { return fullRecording; }
    public void setFullRecording(boolean v)       { this.fullRecording = v; }

    public int getInMeetingCount()                { return inMeetingCount; }
    public void setInMeetingCount(int v)          { this.inMeetingCount = v; }

    public int getOnlineCount()                   { return onlineCount; }
    public void setOnlineCount(int v)             { this.onlineCount = v; }

    public LocalDateTime getStartTime()           { return startTime; }
    public void setStartTime(LocalDateTime v)     { this.startTime = v; }

    public LocalDateTime getEndTime()             { return endTime; }
    public void setEndTime(LocalDateTime v)       { this.endTime = v; }
}
