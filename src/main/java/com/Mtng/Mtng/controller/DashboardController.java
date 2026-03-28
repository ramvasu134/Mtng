package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.model.Recording;
import com.Mtng.Mtng.service.ChatService;
import com.Mtng.Mtng.service.MeetingService;
import com.Mtng.Mtng.service.RecordingService;
import com.Mtng.Mtng.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DashboardController – serves all main UI pages.
 *
 * <p>Routes:
 * <ul>
 *   <li>GET /             → dashboard (Meeting Controls tab)</li>
 *   <li>GET /students     → students list tab</li>
 *   <li>GET /chat         → chat tab</li>
 *   <li>GET /recordings   → recordings tab</li>
 *   <li>GET /create-student → create student tab</li>
 *   <li>GET /docs         → documentation page</li>
 *   <li>GET /userguide    → user guide page</li>
 * </ul>
 * </p>
 */
@Controller
public class DashboardController {

    private final MeetingService meetingService;
    private final StudentService studentService;
    private final ChatService chatService;
    private final RecordingService recordingService;

    @Autowired
    public DashboardController(MeetingService meetingService,
                                StudentService studentService,
                                ChatService chatService,
                                RecordingService recordingService) {
        this.meetingService  = meetingService;
        this.studentService  = studentService;
        this.chatService     = chatService;
        this.recordingService = recordingService;
    }

    // ── Shared model helper ────────────────────────────────────────────────────

    private void addCommonAttributes(Model model) {
        Optional<Meeting> activeMeeting = meetingService.getActiveMeeting();
        activeMeeting.ifPresent(m -> model.addAttribute("activeMeeting", m));
        model.addAttribute("meetingActive",   activeMeeting.isPresent());
        model.addAttribute("totalStudents",   studentService.getTotalCount());
        model.addAttribute("onlineStudents",  studentService.getOnlineCount());
        model.addAttribute("inMeetingCount",  activeMeeting.map(Meeting::getInMeetingCount).orElse(0));
    }

    // ── Pages ─────────────────────────────────────────────────────────────────

    /** Landing page – Meeting Controls */
    @GetMapping("/")
    public String dashboard(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activeTab", "meeting");
        model.addAttribute("meetings", meetingService.getAllMeetings());
        return "dashboard";
    }

    /** Students List page */
    @GetMapping("/students")
    public String students(@RequestParam(required = false) String search, Model model) {
        addCommonAttributes(model);
        model.addAttribute("activeTab", "students");
        model.addAttribute("students",
                search != null && !search.isBlank()
                        ? studentService.search(search)
                        : studentService.getAllStudents());
        model.addAttribute("searchKeyword", search != null ? search : "");
        return "students";
    }

    /** Chat page */
    @GetMapping("/chat")
    public String chat(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activeTab", "chat");
        Optional<Meeting> active = meetingService.getActiveMeeting();
        model.addAttribute("messages",
                active.map(m -> chatService.getMessages(m.getId()))
                      .orElse(chatService.getAllMessages()));
        return "chat";
    }

    /** Create Student page */
    @GetMapping("/create-student")
    public String createStudentPage(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activeTab", "create");
        return "create-student";
    }

    /** Recordings page */
    @GetMapping("/recordings")
    public String recordings(@RequestParam(required = false) String search, Model model) {
        addCommonAttributes(model);
        model.addAttribute("activeTab", "recordings");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        List<Recording> allRecs;
        if (isAdmin) {
            allRecs = recordingService.getAllRecordings();
        } else {
            // Normal user: show only their own recordings
            String username = auth != null ? auth.getName() : "";
            allRecs = recordingService.getRecordingsByParticipantName(username);
            if (allRecs.isEmpty()) {
                // Try matching by display name via student lookup
                allRecs = studentService.findByUsername(username)
                        .map(s -> recordingService.getRecordingsByParticipantName(s.getName()))
                        .orElse(Collections.emptyList());
            }
        }

        // Legacy grouping by studentId
        Map<Long, List<Recording>> recsByStudent = allRecs.stream()
                .collect(Collectors.groupingBy(Recording::getStudentId));

        // Primary grouping by participant name – works for host + participant recordings
        Map<String, List<Recording>> recsByParticipant = allRecs.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStudentName() != null && !r.getStudentName().isBlank()
                                ? r.getStudentName() : "Unknown",
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("students",          studentService.getAllStudents());
        model.addAttribute("recsByStudent",     recsByStudent);
        model.addAttribute("recsByParticipant", recsByParticipant);
        model.addAttribute("allRecordings",     allRecs);
        return "recordings";
    }

    /** Documentation page */
    @GetMapping("/docs")
    public String documentation(Model model) {
        model.addAttribute("activeTab", "docs");
        return "documentation";
    }

    /** User Guide page */
    @GetMapping("/userguide")
    public String userGuide(Model model) {
        model.addAttribute("activeTab", "userguide");
        return "userguide";
    }

    /** Access Denied page */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        addCommonAttributes(model);
        return "access-denied";
    }

    /** Meeting Room page – for students to join/view meeting */
    @GetMapping("/meeting-room")
    public String meetingRoom(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activeTab", "meeting-room");
        // Pass current username to JS
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = (auth != null) ? auth.getName() : "Guest";
        model.addAttribute("currentUser", currentUser);
        
        Optional<Meeting> active = meetingService.getActiveMeeting();
        if (active.isPresent()) {
            Meeting m = active.get();
            model.addAttribute("meeting", m);
            model.addAttribute("hasActiveMeeting", true);
            model.addAttribute("roomName", m.getRoomName() != null ? m.getRoomName() : "");
        } else {
            // Provide a dummy meeting object so Thymeleaf doesn't NPE
            Meeting empty = new Meeting();
            empty.setTitle("No Meeting");
            empty.setRoomName("");
            model.addAttribute("meeting", empty);
            model.addAttribute("hasActiveMeeting", false);
            model.addAttribute("roomName", "");
        }
        return "meeting-room";
    }
}

