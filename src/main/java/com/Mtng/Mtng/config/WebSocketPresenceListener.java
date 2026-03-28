package com.Mtng.Mtng.config;

import com.Mtng.Mtng.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * WebSocketPresenceListener – automatically marks students ONLINE when they
 * connect to a WebSocket (meeting room) and OFFLINE when they disconnect.
 *
 * <p>This supplements the HTTP login-based online status tracking by providing
 * real-time presence detection via WebSocket lifecycle events.</p>
 */
@Component
public class WebSocketPresenceListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPresenceListener.class);

    private final StudentService studentService;

    public WebSocketPresenceListener(StudentService studentService) {
        this.studentService = studentService;
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        try {
            StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
            Principal principal = sha.getUser();
            if (principal != null) {
                String username = principal.getName();
                studentService.markOnline(username);
                log.info("[PRESENCE] WebSocket connected: {} → marked ONLINE", username);
            }
        } catch (Exception e) {
            log.debug("WebSocket connect presence update failed: {}", e.getMessage());
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
            Principal principal = sha.getUser();
            if (principal != null) {
                String username = principal.getName();
                studentService.markOffline(username);
                log.info("[PRESENCE] WebSocket disconnected: {} → marked OFFLINE", username);
            }
        } catch (Exception e) {
            log.debug("WebSocket disconnect presence update failed: {}", e.getMessage());
        }
    }
}

