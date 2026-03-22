package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/** Recording entity – audio recordings linked to a student */
@Entity
@Table(name = "recordings")
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String studentName;

    private int durationSeconds;
    private LocalDate recordingDate;
    private LocalTime recordingTime;

    private Long meetingId;

    @PrePersist
    protected void onCreate() {
        if (recordingDate == null) recordingDate = LocalDate.now();
        if (recordingTime == null) recordingTime = LocalTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public Long getStudentId()                   { return studentId; }
    public void setStudentId(Long studentId)     { this.studentId = studentId; }

    public String getStudentName()               { return studentName; }
    public void setStudentName(String v)         { this.studentName = v; }

    public int getDurationSeconds()              { return durationSeconds; }
    public void setDurationSeconds(int v)        { this.durationSeconds = v; }

    public LocalDate getRecordingDate()          { return recordingDate; }
    public void setRecordingDate(LocalDate v)    { this.recordingDate = v; }

    public LocalTime getRecordingTime()          { return recordingTime; }
    public void setRecordingTime(LocalTime v)    { this.recordingTime = v; }

    public Long getMeetingId()                   { return meetingId; }
    public void setMeetingId(Long meetingId)     { this.meetingId = meetingId; }
}

