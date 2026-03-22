package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** ChatMessage entity – messages sent in the meeting chat */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, length = 1000)
    private String content;

    private LocalDateTime sentAt;

    private Long meetingId;

    @PrePersist
    protected void onCreate() { sentAt = LocalDateTime.now(); }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getSender()                  { return sender; }
    public void setSender(String sender)       { this.sender = sender; }

    public String getContent()                 { return content; }
    public void setContent(String content)     { this.content = content; }

    public LocalDateTime getSentAt()           { return sentAt; }
    public void setSentAt(LocalDateTime v)     { this.sentAt = v; }

    public Long getMeetingId()                 { return meetingId; }
    public void setMeetingId(Long meetingId)   { this.meetingId = meetingId; }
}

