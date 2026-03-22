package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.ChatMessage;
import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.service.ChatService;
import com.Mtng.Mtng.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChatApiControllerTest – unit tests for ChatApiController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatApiController Tests")
class ChatApiControllerTest {

    @Mock private ChatService chatService;
    @Mock private MeetingService meetingService;
    @InjectMocks private ChatApiController controller;

    private Meeting activeMeeting;

    @BeforeEach
    void setUp() {
        activeMeeting = new Meeting();
        activeMeeting.setId(1L);
        activeMeeting.setTitle("Test Meeting");
        activeMeeting.setActive(true);
    }

    @Test
    @DisplayName("messages – returns messages for active meeting")
    void messages_activeMeeting() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("Hello");
        msg.setSender("Teacher");
        when(meetingService.getActiveMeeting()).thenReturn(Optional.of(activeMeeting));
        when(chatService.getMessages(1L)).thenReturn(List.of(msg));

        List<ChatMessage> result = controller.messages();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("messages – returns all messages when no active meeting")
    void messages_noActiveMeeting() {
        when(meetingService.getActiveMeeting()).thenReturn(Optional.empty());
        when(chatService.getAllMessages()).thenReturn(List.of());

        List<ChatMessage> result = controller.messages();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("send – sends message with authenticated sender")
    void send_success() {
        // Set up security context
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getName()).thenReturn("admin");

        ChatMessage saved = new ChatMessage();
        saved.setId(1L);
        saved.setSender("admin");
        saved.setContent("Hello class!");

        when(meetingService.getActiveMeeting()).thenReturn(Optional.of(activeMeeting));
        when(chatService.sendMessage(eq("admin"), eq("Hello class!"), eq(1L))).thenReturn(saved);

        ResponseEntity<ChatMessage> resp = controller.send(Map.of("content", "Hello class!"));
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody().getSender()).isEqualTo("admin");
    }

    @Test
    @DisplayName("send – returns 400 for blank content")
    void send_blankContent() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getName()).thenReturn("admin");

        ResponseEntity<ChatMessage> resp = controller.send(Map.of("content", ""));
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("send – accepts 'message' key for compatibility")
    void send_messageKeyCompat() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getName()).thenReturn("HARI34");

        ChatMessage saved = new ChatMessage();
        saved.setId(2L);
        saved.setSender("HARI34");
        saved.setContent("Hi teacher!");

        when(meetingService.getActiveMeeting()).thenReturn(Optional.of(activeMeeting));
        when(chatService.sendMessage(eq("HARI34"), eq("Hi teacher!"), eq(1L))).thenReturn(saved);

        ResponseEntity<ChatMessage> resp = controller.send(Map.of("message", "Hi teacher!"));
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("clear – clears messages for active meeting")
    void clear_success() {
        when(meetingService.getActiveMeeting()).thenReturn(Optional.of(activeMeeting));
        doNothing().when(chatService).clearMessages(1L);

        ResponseEntity<Void> resp = controller.clear();
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(chatService).clearMessages(1L);
    }
}

