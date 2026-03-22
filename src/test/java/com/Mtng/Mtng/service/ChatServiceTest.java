package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.ChatMessage;
import com.Mtng.Mtng.repository.ChatMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChatServiceTest – unit tests for ChatService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Tests")
class ChatServiceTest {

    @Mock  private ChatMessageRepository chatRepo;
    @InjectMocks private ChatService chatService;

    @Test
    @DisplayName("sendMessage – saves and returns message")
    void sendMessage_savesMessage() {
        ChatMessage saved = new ChatMessage();
        saved.setId(1L);
        saved.setSender("Teacher");
        saved.setContent("Hello class!");
        saved.setMeetingId(1L);

        when(chatRepo.save(any(ChatMessage.class))).thenReturn(saved);

        ChatMessage result = chatService.sendMessage("Teacher", "Hello class!", 1L);

        assertThat(result.getSender()).isEqualTo("Teacher");
        assertThat(result.getContent()).isEqualTo("Hello class!");
        verify(chatRepo).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("getMessages – returns messages for meetingId")
    void getMessages_returnsList() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("Test");
        when(chatRepo.findByMeetingIdOrderBySentAtAsc(1L)).thenReturn(List.of(msg));

        List<ChatMessage> result = chatService.getMessages(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("clearMessages – calls deleteByMeetingId")
    void clearMessages() {
        doNothing().when(chatRepo).deleteByMeetingId(1L);
        chatService.clearMessages(1L);
        verify(chatRepo).deleteByMeetingId(1L);
    }

    @Test
    @DisplayName("getAllMessages – returns all from repo")
    void getAllMessages() {
        when(chatRepo.findAll()).thenReturn(List.of(new ChatMessage()));
        List<ChatMessage> result = chatService.getAllMessages();
        assertThat(result).hasSize(1);
    }
}

