package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.service.MeetingService;
import com.Mtng.Mtng.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * MeetingApiController – REST API for multi-room meeting control.
 * Start/stop rooms: ADMIN only.
 * Join/leave/view: authenticated users.
 */
@RestController
@RequestMapping("/api/meeting")
public class MeetingApiController {

    private static final Logger log = LoggerFactory.getLogger(MeetingApiController.class);

    private final MeetingService meetingService;
    private final StudentService studentService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MeetingApiController(MeetingService meetingService,
                                 StudentService studentService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.meetingService = meetingService;
        this.studentService = studentService;
        this.messagingTemplate = messagingTemplate;
    }

    /** GET /api/meeting/active – backward compat: returns first active meeting */
    @GetMapping("/active")
    public ResponseEntity<?> getActive() {
        Optional<Meeting> m = meetingService.getActiveMeeting();
        return m.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** GET /api/meeting/rooms – list ALL active rooms */
    @GetMapping("/rooms")
    public List<Meeting> getActiveRooms() {
        return meetingService.getActiveMeetings();
    }

    /** GET /api/meeting/room/{roomName} – get a specific active room by name */
    @GetMapping("/room/{roomName}")
    public ResponseEntity<?> getRoom(@PathVariable String roomName) {
        return meetingService.getMeetingByRoomName(roomName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/meeting/start – ADMIN creates a new room with optional participants */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/start")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> start(@RequestBody(required = false) Map<String, Object> body,
                                                      Authentication auth) {
        String title = "Mtng Session";
        List<String> participants = null;

        if (body != null) {
            if (body.containsKey("title")) title = (String) body.get("title");
            if (body.containsKey("participants")) {
                Object p = body.get("participants");
                if (p instanceof List) participants = (List<String>) p;
            }
        }

        String createdBy = auth.getName();
        log.info("[AUDIT] Room start requested: title={}, createdBy={}, participants={}", title, createdBy, participants);

        Meeting m = meetingService.startRoom(title, participants, createdBy);

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room created successfully");
        response.put("meetingId", m.getId());
        response.put("title", m.getTitle());
        response.put("roomName", m.getRoomName());
        response.put("startTime", m.getStartTime().toString());
        response.put("createdBy", m.getCreatedBy());
        // invitedParticipants is a normalised Set<String> – serialises as a JSON array
        response.put("invitedParticipants", m.getInvitedParticipants());

        // Send WebSocket notification to invited participants
        if (participants != null && !participants.isEmpty()) {
            Map<String, Object> notification = Map.of(
                "type", "room-invitation",
                "meetingId", m.getId(),
                "title", m.getTitle(),
                "roomName", m.getRoomName(),
                "createdBy", createdBy,
                "participants", participants
            );
            messagingTemplate.convertAndSend("/topic/room-invitations", notification);
            log.info("[NOTIFY] Room invitation sent for room: {}", m.getRoomName());
        }

        return ResponseEntity.ok(response);
    }

    /** POST /api/meeting/stop – ADMIN stops a specific room */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/stop")
    public ResponseEntity<?> stop(@RequestBody(required = false) Map<String, String> body) {
        try {
            String roomName = (body != null) ? body.get("roomName") : null;

            if (roomName != null && !roomName.isBlank()) {
                log.info("[AUDIT] Room stop requested: roomName={}", roomName);
                meetingService.stopMeetingByRoomName(roomName);

                // Notify participants that room is ending
                messagingTemplate.convertAndSend("/topic/room/" + roomName,
                    Map.of("type", "room-ended", "roomName", roomName));
            } else {
                log.info("[AUDIT] Meeting stop requested (first active)");
                meetingService.stopMeeting();
            }
            return ResponseEntity.ok(Map.of("message", "Room stopped"));
        } catch (Exception e) {
            log.error("Error stopping room: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/meeting/stop-all – ADMIN stops all active rooms */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/stop-all")
    public ResponseEntity<?> stopAll() {
        log.info("[AUDIT] Stop all rooms requested");
        meetingService.stopAllMeetings();
        messagingTemplate.convertAndSend("/topic/room-invitations",
            Map.of("type", "all-rooms-ended"));
        return ResponseEntity.ok(Map.of("message", "All rooms stopped"));
    }

    /** POST /api/meeting/notify-students – notify students of meeting start */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notify-students")
    public ResponseEntity<?> notifyStudents(@RequestBody Map<String, Object> body) {
        Long meetingId = ((Number) body.get("meetingId")).longValue();
        String title = (String) body.get("title");
        log.info("[AUDIT] Students notified for meeting: id={}, title={}", meetingId, title);
        return ResponseEntity.ok(Map.of("message", "Students notified", "meetingId", meetingId));
    }

    /** POST /api/meeting/toggle-recording – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/toggle-recording")
    public ResponseEntity<?> toggleRecording(@RequestBody(required = false) Map<String, Object> body) {
        try {
            log.info("[AUDIT] Recording toggle requested");
            if (body != null && body.containsKey("meetingId")) {
                Long meetingId = ((Number) body.get("meetingId")).longValue();
                return ResponseEntity.ok(meetingService.toggleFullRecording(meetingId));
            }
            return ResponseEntity.ok(meetingService.toggleFullRecording());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/meeting/join – user joins a specific room */
    @PostMapping("/join")
    public ResponseEntity<?> joinMeeting(@RequestBody(required = false) Map<String, String> body,
                                          Authentication auth) {
        try {
            String username = auth.getName();
            String roomName = (body != null) ? body.get("roomName") : null;

            Optional<Meeting> m;
            if (roomName != null && !roomName.isBlank()) {
                m = meetingService.getMeetingByRoomName(roomName);
            } else {
                m = meetingService.getActiveMeeting();
            }

            if (m.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No active room to join"));
            }

            log.info("[AUDIT] User '{}' joining room '{}' (id={})", username, m.get().getRoomName(), m.get().getId());
            meetingService.addStudentToMeeting(m.get().getId(), username);
            studentService.markOnline(username);

            return ResponseEntity.ok(Map.of(
                    "message", "Joined room",
                    "meetingId", m.get().getId(),
                    "roomName", m.get().getRoomName(),
                    "title", m.get().getTitle(),
                    "username", username
            ));
        } catch (Exception e) {
            log.error("Error joining room: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/meeting/leave – user leaves a specific room */
    @PostMapping("/leave")
    public ResponseEntity<?> leaveMeeting(@RequestBody(required = false) Map<String, String> body,
                                           Authentication auth) {
        try {
            String username = auth.getName();
            String roomName = (body != null) ? body.get("roomName") : null;

            if (roomName != null && !roomName.isBlank()) {
                meetingService.getMeetingByRoomName(roomName).ifPresent(m ->
                    meetingService.removeStudentFromMeeting(m.getId(), username));
            } else {
                meetingService.getActiveMeeting().ifPresent(m ->
                    meetingService.removeStudentFromMeeting(m.getId(), username));
            }

            log.info("[AUDIT] User '{}' left room", username);
            return ResponseEntity.ok(Map.of("message", "Left room"));
        } catch (Exception e) {
            log.error("Error leaving room: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
