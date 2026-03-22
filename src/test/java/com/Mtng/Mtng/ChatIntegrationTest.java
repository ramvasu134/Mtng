package com.Mtng.Mtng;

import com.Mtng.Mtng.service.MeetingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChatIntegrationTest – integration tests for Chat API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Chat Integration Tests")
class ChatIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MeetingService meetingService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Send and retrieve chat messages")
    void sendAndRetrieveMessages() throws Exception {
        // Start a meeting
        meetingService.startMeeting("Chat Test");

        // Send a message
        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello everyone!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sender").value("admin"))
                .andExpect(jsonPath("$.content").value("Hello everyone!"));

        // Retrieve messages
        mockMvc.perform(get("/api/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student can send chat messages")
    void studentCanSendChat() throws Exception {
        meetingService.startMeeting("Student Chat Test");

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hi teacher!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sender").value("HARI34"))
                .andExpect(jsonPath("$.content").value("Hi teacher!"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Send with 'message' key for compatibility")
    void sendWithMessageKey() throws Exception {
        meetingService.startMeeting("Compat Chat Test");

        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Meeting room message\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Meeting room message"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Cannot send blank message")
    void cannotSendBlankMessage() throws Exception {
        mockMvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can clear chat")
    void adminCanClearChat() throws Exception {
        meetingService.startMeeting("Clear Chat Test");

        // Send a message first
        mockMvc.perform(post("/api/chat/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"To be cleared\"}"));

        // Clear
        mockMvc.perform(delete("/api/chat/clear"))
                .andExpect(status().isOk());
    }
}

