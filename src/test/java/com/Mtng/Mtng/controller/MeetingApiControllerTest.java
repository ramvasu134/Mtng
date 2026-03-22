package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.service.MeetingService;
import com.Mtng.Mtng.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MeetingApiControllerTest – unit tests for MeetingApiController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingApiController Tests")
class MeetingApiControllerTest {

    @Mock private MeetingService meetingService;
    @Mock private StudentService studentService;
    @Mock private Authentication authentication;
    @InjectMocks private MeetingApiController controller;

    private Meeting activeMeeting;

    @BeforeEach
    void setUp() {
        activeMeeting = new Meeting();
        activeMeeting.setId(1L);
        activeMeeting.setTitle("Test Meeting");
        activeMeeting.setRoomName("mtng-abc12345");
        activeMeeting.setActive(true);
        activeMeeting.setStartTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("getActive – returns meeting when active")
    void getActive_returnsMeeting() {
        when(meetingService.getActiveMeeting()).thenReturn(Optional.of(activeMeeting));
        ResponseEntity<?> resp = controller.getActive();
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
    }

    @Test
    @DisplayName("getActive – returns 204 when no active meeting")
    void getActive_returns204WhenNoMeeting() {
        when(meetingService.getActiveMeeting()).thenReturn(Optional.empty());
        ResponseEntity<?> resp = controller.getActive();
        assertThat(resp.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    @DisplayName("start – creates meeting and returns details with roomName")
    void start_createsMeeting() {
        when(meetingService.startMeeting(anyString())).thenReturn(activeMeeting);
        Map<String, String> body = Map.of("title", "Test Session");
        ResponseEntity<Map<String, Object>> resp = controller.start(body);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).containsKey("roomName");
        assertThat(resp.getBody().get("roomName")).isEqualTo("mtng-abc12345");
        assertThat(resp.getBody()).containsKey("meetingId");
    }

    @Test
    @DisplayName("start – uses default title when body is null")
    void start_defaultTitle() {
        when(meetingService.startMeeting("Mtng Session")).thenReturn(activeMeeting);
        ResponseEntity<Map<String, Object>> resp = controller.start(null);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(meetingService).startMeeting("Mtng Session");
    }

    @Test
    @DisplayName("stop – stops meeting successfully")
    void stop_success() {
        doNothing().when(meetingService).stopMeeting();
        ResponseEntity<?> resp = controller.stop();
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(meetingService).stopMeeting();
    }

    @Test
    @DisplayName("stop – returns 400 on error")
    void stop_error() {
        doThrow(new RuntimeException("Test error")).when(meetingService).stopMeeting();
        ResponseEntity<?> resp = controller.stop();
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("joinMeeting – adds user to active meeting")
    void join_success() {
        when(authentication.getName()).thenReturn("HARI34");
        when(meetingService.getActiveMeeting()).thenReturn(Optional.of(activeMeeting));
        doNothing().when(meetingService).addStudentToMeeting(anyLong(), anyString());
        doNothing().when(studentService).markOnline(anyString());

        ResponseEntity<?> resp = controller.joinMeeting(authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body).containsKey("roomName");
        assertThat(body.get("username")).isEqualTo("HARI34");
    }

    @Test
    @DisplayName("joinMeeting – returns 400 when no active meeting")
    void join_noActiveMeeting() {
        when(authentication.getName()).thenReturn("HARI34");
        when(meetingService.getActiveMeeting()).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.joinMeeting(authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("leaveMeeting – removes user from meeting")
    void leave_success() {
        when(authentication.getName()).thenReturn("HARI34");
        when(meetingService.getActiveMeeting()).thenReturn(Optional.of(activeMeeting));
        doNothing().when(meetingService).removeStudentFromMeeting(anyLong(), anyString());

        ResponseEntity<?> resp = controller.leaveMeeting(authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(meetingService).removeStudentFromMeeting(1L, "HARI34");
    }

    @Test
    @DisplayName("leaveMeeting – handles no active meeting gracefully")
    void leave_noActiveMeeting() {
        when(authentication.getName()).thenReturn("HARI34");
        when(meetingService.getActiveMeeting()).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.leaveMeeting(authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("toggleRecording – returns meeting with toggled recording")
    void toggleRecording_success() {
        activeMeeting.setFullRecording(true);
        when(meetingService.toggleFullRecording()).thenReturn(activeMeeting);
        ResponseEntity<?> resp = controller.toggleRecording();
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("toggleRecording – returns 400 when no active meeting")
    void toggleRecording_noMeeting() {
        when(meetingService.toggleFullRecording())
                .thenThrow(new IllegalStateException("No active meeting"));
        ResponseEntity<?> resp = controller.toggleRecording();
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }
}

