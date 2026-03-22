package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.ChatMessage;
import com.Mtng.Mtng.service.ChatService;
import com.Mtng.Mtng.service.MeetingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * ChatApiController – REST API for meeting chat messages.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private static final Logger log = LoggerFactory.getLogger(ChatApiController.class);

    private final ChatService chatService;
    private final MeetingService meetingService;

    @Autowired
    public ChatApiController(ChatService chatService, MeetingService meetingService) {
        this.chatService    = chatService;
        this.meetingService = meetingService;
    }

    /** GET /api/chat/messages */
    @GetMapping("/messages")
    public List<ChatMessage> messages() {
        return meetingService.getActiveMeeting()
                .map(m -> chatService.getMessages(m.getId()))
                .orElse(chatService.getAllMessages());
    }

    /** POST /api/chat/send – uses authenticated user's name as sender */
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> send(@RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String sender = (auth != null) ? auth.getName() : body.getOrDefault("sender", "Unknown");
        String content = body.get("content");
        // Also check 'message' key for compatibility with meeting-room chat
        if ((content == null || content.isBlank()) && body.containsKey("message")) {
            content = body.get("message");
        }
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Long meetingId = meetingService.getActiveMeeting()
                .map(m -> m.getId()).orElse(0L);
        log.info("[AUDIT] Chat message from '{}' in meeting {}", sender, meetingId);
        return ResponseEntity.ok(chatService.sendMessage(sender, content, meetingId));
    }

    /** DELETE /api/chat/clear */
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear() {
        log.info("[AUDIT] Chat cleared");
        meetingService.getActiveMeeting()
                .ifPresent(m -> chatService.clearMessages(m.getId()));
        return ResponseEntity.ok().build();
    }
}
