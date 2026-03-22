package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.ChatMessage;
import com.Mtng.Mtng.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ChatService – manages chat messages for a meeting session.
 */
@Service
@Transactional
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatRepo;

    @Autowired
    public ChatService(ChatMessageRepository chatRepo) {
        this.chatRepo = chatRepo;
    }

    /** Post a new message */
    public ChatMessage sendMessage(String sender, String content, Long meetingId) {
        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setContent(content);
        msg.setMeetingId(meetingId);
        ChatMessage saved = chatRepo.save(msg);
        log.info("[AUDIT] Chat: sender={}, meetingId={}, contentLength={}", sender, meetingId, content.length());
        return saved;
    }

    /** Retrieve all messages for a meeting (chronological) */
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Long meetingId) {
        return chatRepo.findByMeetingIdOrderBySentAtAsc(meetingId);
    }

    /** Clear all messages for a meeting */
    public void clearMessages(Long meetingId) {
        chatRepo.deleteByMeetingId(meetingId);
    }

    /** Get all messages (for dashboard with no active meeting) */
    @Transactional(readOnly = true)
    public List<ChatMessage> getAllMessages() {
        return chatRepo.findAll();
    }
}

