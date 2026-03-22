package com.Mtng.Mtng;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SecurityIntegrationTest – tests security access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated user redirected to login")
    void unauthenticatedRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can access dashboard")
    void adminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student can access dashboard")
    void studentCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can access create-student page")
    void adminCanAccessCreateStudent() throws Exception {
        mockMvc.perform(get("/create-student"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student cannot access create-student page (redirected)")
    void studentCannotAccessCreateStudent() throws Exception {
        // Spring Security redirects unauthorized page requests (302) rather than 403
        mockMvc.perform(get("/create-student"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can access meeting room")
    void adminCanAccessMeetingRoom() throws Exception {
        mockMvc.perform(get("/meeting-room"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student can access meeting room")
    void studentCanAccessMeetingRoom() throws Exception {
        mockMvc.perform(get("/meeting-room"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can access students list")
    void adminCanAccessStudentsList() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can access recordings page")
    void adminCanAccessRecordings() throws Exception {
        mockMvc.perform(get("/recordings"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin can access chat page")
    void adminCanAccessChat() throws Exception {
        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login page is publicly accessible")
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student cannot stop a meeting (403)")
    void studentCannotStopMeeting() throws Exception {
        mockMvc.perform(post("/api/meeting/stop"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "HARI34", roles = {"USER"})
    @DisplayName("Student cannot toggle recording (403)")
    void studentCannotToggleRecording() throws Exception {
        mockMvc.perform(post("/api/meeting/toggle-recording"))
                .andExpect(status().isForbidden());
    }
}

