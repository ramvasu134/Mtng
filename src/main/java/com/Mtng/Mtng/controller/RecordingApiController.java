package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Recording;
import com.Mtng.Mtng.service.RecordingService;
import com.Mtng.Mtng.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * RecordingApiController – REST API for recordings management.
 * Read: all authenticated. Write/Delete: ADMIN only.
 */
@RestController
@RequestMapping("/api/recordings")
public class RecordingApiController {

    private final RecordingService recordingService;
    private final StudentService   studentService;

    @Autowired
    public RecordingApiController(RecordingService recordingService, StudentService studentService) {
        this.recordingService = recordingService;
        this.studentService   = studentService;
    }

    /** GET /api/recordings – all recordings (anyone authenticated) */
    @GetMapping
    public List<Recording> list() {
        return recordingService.getAllRecordings();
    }

    /** GET /api/recordings/student/{id} (anyone authenticated) */
    @GetMapping("/student/{id}")
    public List<Recording> byStudent(@PathVariable Long id) {
        return recordingService.getStudentRecordings(id);
    }

    /** POST /api/recordings – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Recording> add(@RequestBody Map<String, Object> body) {
        Long   studentId = Long.parseLong(body.get("studentId").toString());
        String name      = studentService.findById(studentId)
                .map(s -> s.getName()).orElse("Unknown");
        int    duration  = Integer.parseInt(body.getOrDefault("duration", "6").toString());
        Long   meetingId = body.containsKey("meetingId")
                ? Long.parseLong(body.get("meetingId").toString()) : 0L;
        return ResponseEntity.ok(recordingService.addRecording(studentId, name, duration, meetingId));
    }

    /** DELETE /api/recordings/{id} – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recordingService.deleteRecording(id);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/recordings/clear – ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAll() {
        recordingService.clearAll();
        return ResponseEntity.ok().build();
    }
}
