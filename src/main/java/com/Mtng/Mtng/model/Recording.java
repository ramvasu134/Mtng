package com.Mtng.Mtng.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/** Recording entity – audio recordings linked to a meeting session */
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

    /** Original file name of the saved recording (e.g. mtng-recording-2026-03-26.webm) */
    private String fileName;

    /** Role of the participant: HOST or PARTICIPANT */
    private String participantType;

    /** Size of the recording file in bytes */
    private Long fileSizeBytes;

    /** Whether this session was saved to the database */
    private boolean savedToDatabase;

    /**
     * Actual audio bytes stored in the database.
     * Annotated @JsonIgnore so it is never serialised in list/detail APIs.
     * Streamed separately via GET /api/recordings/{id}/audio.
     */
    @Lob
    @Column(columnDefinition = "BLOB")
    @JsonIgnore
    private byte[] audioData;

    /** MIME type of the stored audio (e.g. audio/webm;codecs=opus) */
    private String audioMimeType;

    @PrePersist
    protected void onCreate() {
        if (recordingDate == null) recordingDate = LocalDate.now();
        if (recordingTime == null) recordingTime = LocalTime.now();
        if (participantType == null) participantType = "PARTICIPANT";
        if (fileSizeBytes == null) fileSizeBytes = 0L;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public Long getStudentId()                   { return studentId; }
    public void setStudentId(Long v)             { this.studentId = v; }
    public String getStudentName()               { return studentName; }
    public void setStudentName(String v)         { this.studentName = v; }
    public int getDurationSeconds()              { return durationSeconds; }
    public void setDurationSeconds(int v)        { this.durationSeconds = v; }
    public LocalDate getRecordingDate()          { return recordingDate; }
    public void setRecordingDate(LocalDate v)    { this.recordingDate = v; }
    public LocalTime getRecordingTime()          { return recordingTime; }
    public void setRecordingTime(LocalTime v)    { this.recordingTime = v; }
    public Long getMeetingId()                   { return meetingId; }
    public void setMeetingId(Long v)             { this.meetingId = v; }
    public String getFileName()                  { return fileName; }
    public void setFileName(String v)            { this.fileName = v; }
    public String getParticipantType()           { return participantType; }
    public void setParticipantType(String v)     { this.participantType = v; }
    public Long getFileSizeBytes()               { return fileSizeBytes; }
    public void setFileSizeBytes(Long v)         { this.fileSizeBytes = v; }
    public boolean isSavedToDatabase()           { return savedToDatabase; }
    public void setSavedToDatabase(boolean v)    { this.savedToDatabase = v; }
    public byte[] getAudioData()                 { return audioData; }
    public void setAudioData(byte[] v)           { this.audioData = v; }
    public String getAudioMimeType()             { return audioMimeType; }
    public void setAudioMimeType(String v)       { this.audioMimeType = v; }

    /** Convenience: true if actual audio bytes are stored */
    public boolean hasAudio()                    { return audioData != null && audioData.length > 0; }
}
