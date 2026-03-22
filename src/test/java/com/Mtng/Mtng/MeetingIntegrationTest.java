package com.Mtng.Mtng;

import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.model.Student;
import com.Mtng.Mtng.repository.MeetingRepository;
import com.Mtng.Mtng.repository.StudentRepository;
import com.Mtng.Mtng.service.MeetingService;
import com.Mtng.Mtng.service.StudentService;
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
 * MeetingIntegrationTest – full integration tests with MockMvc.
 * Tests the complete request/response lifecycle through Spring MVC.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Meeting Integration Tests")
class MeetingIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MeetingService meetingService;
    @Autowired private MeetingRepository meetingRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can start a meeting via API")
    void adminCanStartMeeting() throws Exception {
        mockMvc.perform(post("/api/meeting/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Integration Test Meeting\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingId").exists())
                .andExpect(jsonPath("$.roomName").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Meeting"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can stop a meeting via API")
    void adminCanStopMeeting() throws Exception {
        // Start a meeting first
        meetingService.startMeeting("Stop Test");

        mockMvc.perform(post("/api/meeting/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Meeting stopped"));
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student cannot start a meeting (403)")
    void studentCannotStartMeeting() throws Exception {
        mockMvc.perform(post("/api/meeting/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Student Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student can join an active meeting")
    void studentCanJoinMeeting() throws Exception {
        // Start a meeting as setup
        meetingService.startMeeting("Join Test");

        mockMvc.perform(post("/api/meeting/join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingId").exists())
                .andExpect(jsonPath("$.roomName").exists())
                .andExpect(jsonPath("$.username").value("HARI34"));
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student gets 400 when joining with no active meeting")
    void studentCannotJoinNoMeeting() throws Exception {
        // Ensure no active meetings
        meetingRepository.findByActive(true).forEach(m -> {
            m.setActive(false);
            meetingRepository.save(m);
        });

        mockMvc.perform(post("/api/meeting/join"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No active meeting to join"));
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student can leave a meeting")
    void studentCanLeaveMeeting() throws Exception {
        meetingService.startMeeting("Leave Test");

        mockMvc.perform(post("/api/meeting/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Left meeting"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Get active meeting returns the current meeting")
    void getActiveMeeting() throws Exception {
        meetingService.startMeeting("Active Test");

        mockMvc.perform(get("/api/meeting/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Active Test"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.roomName").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Toggle recording on active meeting")
    void toggleRecording() throws Exception {
        meetingService.startMeeting("Record Test");

        mockMvc.perform(post("/api/meeting/toggle-recording"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullRecording").value(true));
    }
}

