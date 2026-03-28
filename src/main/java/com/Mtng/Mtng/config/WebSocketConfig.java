package com.Mtng.Mtng.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig – configures STOMP over WebSocket for WebRTC signaling.
 *
 * <p>This replaces the Jitsi Meet public server dependency entirely.
 * All signaling (offer/answer/ICE candidates) happens through Spring's
 * in-memory STOMP broker, requiring no external service or authentication.</p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>/ws          – SockJS WebSocket endpoint</li>
 *   <li>/topic/room/{roomId} – broadcast topic per meeting room</li>
 *   <li>/app/signal/{roomId} – inbound signaling messages</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // In-memory broker – topics for room broadcasts
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages from client → server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // /ws endpoint with SockJS fallback (works even if WebSocket is blocked)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

