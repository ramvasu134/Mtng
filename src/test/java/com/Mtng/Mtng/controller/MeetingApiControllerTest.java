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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MeetingApiControllerTest – unit tests for MeetingApiController (multi-room).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingApiController Tests")
class MeetingApiControllerTest {

    @Mock private MeetingService meetingService;
    @Mock private StudentService studentService;
    @Mock private SimpMessagingTemplate messagingTemplate;
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
        activeMeeting.setCreatedBy("admin");
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
    @DisplayName("start – creates room and returns details with roomName")
    void start_createsRoom() {
        when(authentication.getName()).thenReturn("admin");
        when(meetingService.startRoom(anyString(), any(), anyString())).thenReturn(activeMeeting);
        Map<String, Object> body = Map.of("title", "Test Session");
        ResponseEntity<Map<String, Object>> resp = controller.start(body, authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).containsKey("roomName");
        assertThat(resp.getBody().get("roomName")).isEqualTo("mtng-abc12345");
    }

    @Test
    @DisplayName("start – uses default title when body is null")
    void start_defaultTitle() {
        when(authentication.getName()).thenReturn("admin");
        when(meetingService.startRoom(eq("Mtng Session"), isNull(), eq("admin"))).thenReturn(activeMeeting);
        ResponseEntity<Map<String, Object>> resp = controller.start(null, authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("stop – stops room by roomName")
    void stop_success() {
        doNothing().when(meetingService).stopMeetingByRoomName("mtng-abc12345");
        Map<String, String> body = Map.of("roomName", "mtng-abc12345");
        ResponseEntity<?> resp = controller.stop(body);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(meetingService).stopMeetingByRoomName("mtng-abc12345");
    }

    @Test
    @DisplayName("stop – fallback stops first active when no roomName")
    void stop_fallback() {
        doNothing().when(meetingService).stopMeeting();
        ResponseEntity<?> resp = controller.stop(null);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(meetingService).stopMeeting();
    }

    @Test
    @DisplayName("joinMeeting – adds user to room by roomName")
    void join_success() {
        when(authentication.getName()).thenReturn("HARI34");
        when(meetingService.getMeetingByRoomName("mtng-abc12345")).thenReturn(Optional.of(activeMeeting));
        doNothing().when(meetingService).addStudentToMeeting(anyLong(), anyString());
        doNothing().when(studentService).markOnline(anyString());

        Map<String, String> body = Map.of("roomName", "mtng-abc12345");
        ResponseEntity<?> resp = controller.joinMeeting(body, authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, Object> respBody = (Map<String, Object>) resp.getBody();
        assertThat(respBody).containsKey("roomName");
        assertThat(respBody.get("username")).isEqualTo("HARI34");
    }

    @Test
    @DisplayName("joinMeeting – returns 400 when no room found")
    void join_noRoom() {
        when(authentication.getName()).thenReturn("HARI34");
        when(meetingService.getMeetingByRoomName("invalid")).thenReturn(Optional.empty());

        Map<String, String> body = Map.of("roomName", "invalid");
        ResponseEntity<?> resp = controller.joinMeeting(body, authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("leaveMeeting – removes user from specific room")
    void leave_success() {
        when(authentication.getName()).thenReturn("HARI34");
        when(meetingService.getMeetingByRoomName("mtng-abc12345")).thenReturn(Optional.of(activeMeeting));
        doNothing().when(meetingService).removeStudentFromMeeting(anyLong(), anyString());

        Map<String, String> body = Map.of("roomName", "mtng-abc12345");
        ResponseEntity<?> resp = controller.leaveMeeting(body, authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("leaveMeeting – handles null body gracefully")
    void leave_nullBody() {
        when(authentication.getName()).thenReturn("HARI34");
        when(meetingService.getActiveMeeting()).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.leaveMeeting(null, authentication);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("toggleRecording – returns meeting with toggled recording")
    void toggleRecording_success() {
        activeMeeting.setFullRecording(true);
        when(meetingService.toggleFullRecording()).thenReturn(activeMeeting);
        ResponseEntity<?> resp = controller.toggleRecording(null);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("toggleRecording – returns 400 when no active meeting")
    void toggleRecording_noMeeting() {
        when(meetingService.toggleFullRecording())
                .thenThrow(new IllegalStateException("No active meeting"));
        ResponseEntity<?> resp = controller.toggleRecording(null);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }
}

