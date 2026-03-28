package com.Mtng.Mtng.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ChatMessage entity – messages sent in the meeting chat.
 *
 * <p>DB design notes:</p>
 * <ul>
 *   <li>meetingId is a soft FK (plain Long) – messages persist after meeting ends.</li>
 *   <li>Composite index on (meeting_id, sent_at) covers the primary query:
 *       findByMeetingIdOrderBySentAtAsc.</li>
 *   <li>VARCHAR(1000) for content; extend if needed.</li>
 * </ul>
 */
@Entity
@Table(
    name = "chat_messages",
    indexes = {
        @Index(name = "idx_chat_meeting_sent", columnList = "meeting_id, sent_at"),
        @Index(name = "idx_chat_sent_at",      columnList = "sent_at")
    }
)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /** Soft FK to meetings.id – messages are kept after the meeting ends. */
    @Column(name = "meeting_id")
    private Long meetingId;

    @PrePersist
    protected void onCreate() { if (sentAt == null) sentAt = LocalDateTime.now(); }

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
