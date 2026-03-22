package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.service.MeetingService;
import com.Mtng.Mtng.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

/**
 * MeetingApiController – REST API for meeting control.
 * Start/stop/toggle-recording: ADMIN only. 
 * Join/leave/view: authenticated users.
 */
@RestController
@RequestMapping("/api/meeting")
public class MeetingApiController {

    private static final Logger log = LoggerFactory.getLogger(MeetingApiController.class);

    private final MeetingService meetingService;
    private final StudentService studentService;

    @Autowired
    public MeetingApiController(MeetingService meetingService, StudentService studentService) {
        this.meetingService = meetingService;
        this.studentService = studentService;
    }

    /** GET /api/meeting/active – anyone authenticated */
    @GetMapping("/active")
    public ResponseEntity<?> getActive() {
        Optional<Meeting> m = meetingService.getActiveMeeting();
        return m.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** POST /api/meeting/start – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start(@RequestBody(required = false) Map<String, String> body) {
        String title = (body != null && body.containsKey("title")) ? body.get("title") : "Mtng Session";
        log.info("[AUDIT] Meeting start requested: title={}", title);
        Meeting m = meetingService.startMeeting(title);
        
        // Return meeting details for auto-redirect and notifications
        return ResponseEntity.ok(Map.of(
                "message", "Meeting started successfully",
                "meetingId", m.getId(),
                "title", m.getTitle(),
                "roomName", m.getRoomName(),
                "startTime", m.getStartTime().toString(),
                "redirect", "/meeting-room",
                "notifyStudents", true
        ));
    }

    /** POST /api/meeting/notify-students – notify all students of meeting start */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notify-students")
    public ResponseEntity<?> notifyStudents(@RequestBody Map<String, Object> body) {
        Long meetingId = ((Number) body.get("meetingId")).longValue();
        String title = (String) body.get("title");
        log.info("[AUDIT] Students notified for meeting: id={}, title={}", meetingId, title);
        // In a real system, this would send notifications via WebSocket, SSE, or push notifications
        return ResponseEntity.ok(Map.of("message", "Students notified", "meetingId", meetingId));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/stop")
    public ResponseEntity<?> stop() {
        try {
            log.info("[AUDIT] Meeting stop requested");
            meetingService.stopMeeting();
            return ResponseEntity.ok(Map.of("message", "Meeting stopped"));
        } catch (Exception e) {
            log.error("Error stopping meeting: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/meeting/toggle-recording – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/toggle-recording")
    public ResponseEntity<?> toggleRecording() {
        try {
            log.info("[AUDIT] Recording toggle requested");
            return ResponseEntity.ok(meetingService.toggleFullRecording());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/meeting/join – student joins meeting */
    @PostMapping("/join")
    public ResponseEntity<?> joinMeeting(Authentication auth) {
        try {
            String username = auth.getName();
            log.info("[AUDIT] User '{}' attempting to join meeting", username);
            Optional<Meeting> m = meetingService.getActiveMeeting();
            if (m.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No active meeting to join"));
            }
            meetingService.addStudentToMeeting(m.get().getId(), username);
            studentService.markOnline(username);
            return ResponseEntity.ok(Map.of(
                    "message", "Joined meeting",
                    "meetingId", m.get().getId(),
                    "roomName", m.get().getRoomName(),
                    "username", username
            ));
        } catch (Exception e) {
            log.error("Error joining meeting: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/meeting/leave – student leaves meeting */
    @PostMapping("/leave")
    public ResponseEntity<?> leaveMeeting(Authentication auth) {
        try {
            String username = auth.getName();
            log.info("[AUDIT] User '{}' leaving meeting", username);
            Optional<Meeting> m = meetingService.getActiveMeeting();
            if (m.isPresent()) {
                meetingService.removeStudentFromMeeting(m.get().getId(), username);
            }
            return ResponseEntity.ok(Map.of("message", "Left meeting"));
        } catch (Exception e) {
            log.error("Error leaving meeting: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
