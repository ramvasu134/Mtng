package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.SignalMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.security.Principal;

/**
 * SignalingController – routes WebRTC signaling messages between peers.
 *
 * <p>All messages sent to /app/signal/{roomId} are broadcast to all
 * subscribers of /topic/room/{roomId}. Each client filters by the
 * "to" field (null = intended for all, or a specific username).</p>
 *
 * <p>This completely replaces Jitsi's public server – no external
 * authentication or third-party dependency required.</p>
 */
@Controller
public class SignalingController {

    /**
     * Handle a WebRTC signaling message and broadcast it to all room participants.
     *
     * @param roomId   the meeting room identifier (e.g. "mtng-abc12345")
     * @param message  the signaling message (join/offer/answer/ice-candidate/leave)
     * @param principal the authenticated user (auto-injected by Spring Security + WebSocket)
     * @return the message broadcast to /topic/room/{roomId}
     */
    @MessageMapping("/signal/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public SignalMessage signal(@DestinationVariable String roomId,
                                SignalMessage message,
                                Principal principal) {
        // Stamp the sender's username from the server-side session
        if (principal != null && (message.getFrom() == null || message.getFrom().isBlank())) {
            message.setFrom(principal.getName());
        }
        return message;
    }
}

