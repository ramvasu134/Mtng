package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Student;
import com.Mtng.Mtng.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * StudentApiController – REST API for student CRUD and actions.
 *
 * <p>Read endpoints: accessible to all authenticated users.
 * Write endpoints (create/update/delete/block/mute): ADMIN only.</p>
 */
@RestController
@RequestMapping("/api/students")
public class StudentApiController {

    private final StudentService studentService;

    @Autowired
    public StudentApiController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** GET /api/students – list all students (all authenticated users) */
    @GetMapping
    public List<Student> list() {
        return studentService.getAllStudents();
    }

    /** GET /api/students/online – count of online students */
    @GetMapping("/online")
    public java.util.Map<String, Object> onlineCount() {
        long online = studentService.getOnlineCount();
        long total  = studentService.getTotalCount();
        return java.util.Map.of("online", online, "total", total);
    }

    /** GET /api/students/online-list – list of online students (for room creation) */
    @GetMapping("/online-list")
    public List<Student> onlineList() {
        return studentService.getOnlineStudents();
    }

    /** GET /api/students/{id} (all authenticated users) */
    @GetMapping("/{id}")
    public ResponseEntity<Student> get(@PathVariable Long id) {
        return studentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/students – create (ADMIN only) */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Student student) {
        try {
            Student saved = studentService.createStudent(student);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/students/{id} – update (ADMIN only) */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Student student) {
        try {
            return ResponseEntity.ok(studentService.updateStudent(id, student));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/students/{id} (ADMIN only) */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/students/{id}/block – toggle block (ADMIN only) */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/block")
    public ResponseEntity<Student> block(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.toggleBlock(id));
    }

    /** POST /api/students/{id}/mute – toggle mute (ADMIN only) */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/mute")
    public ResponseEntity<Student> mute(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.toggleMute(id));
    }

    /** POST /api/students/heartbeat – called by students periodically to stay ONLINE */
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails) {
        if (userDetails != null) {
            studentService.markOnline(userDetails.getUsername());
            return ResponseEntity.ok(Map.of("status", "ONLINE"));
        }
        return ResponseEntity.ok(Map.of("status", "ignored"));
    }
}
