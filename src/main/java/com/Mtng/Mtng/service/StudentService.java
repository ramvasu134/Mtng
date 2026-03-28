package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.Student;
import com.Mtng.Mtng.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * StudentService – business logic for student management.
 *
 * <p>Covers: create, edit, delete, block, mute, search students.</p>
 */
@Service
@Transactional
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final PasswordEncoder   passwordEncoder;

    @Autowired
    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    /** Return all students ordered by name */
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /** Find single student by ID */
    @Transactional(readOnly = true)
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    /** Find student by username */
    @Transactional(readOnly = true)
    public Optional<Student> findByUsername(String username) {
        return studentRepository.findByUsername(username);
    }

    /** Search students by name or username */
    @Transactional(readOnly = true)
    public List<Student> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllStudents();
        return studentRepository
                .findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(keyword, keyword);
    }

    /** Create a new student; throws if username already taken */
    public Student createStudent(Student student) {
        if (studentRepository.existsByUsername(student.getUsername())) {
            log.warn("Attempt to create student with duplicate username: {}", student.getUsername());
            throw new IllegalArgumentException("Username already exists: " + student.getUsername());
        }
        String raw = student.getPassword();
        student.setRawPassword(raw);
        student.setPassword(passwordEncoder.encode(raw));
        Student saved = studentRepository.save(student);
        log.info("[AUDIT] Student created: id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    /** Update an existing student */
    public Student updateStudent(Long id, Student updated) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));
        existing.setName(updated.getName());
        existing.setDeviceLock(updated.isDeviceLock());
        existing.setShowRecordings(updated.isShowRecordings());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            String raw = updated.getPassword();
            existing.setRawPassword(raw);
            existing.setPassword(passwordEncoder.encode(raw));
        }
        return studentRepository.save(existing);
    }

    /** Toggle blocked status */
    public Student toggleBlock(Long id) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));
        s.setBlocked(!s.isBlocked());
        return studentRepository.save(s);
    }

    /** Toggle muted status */
    public Student toggleMute(Long id) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));
        s.setMuted(!s.isMuted());
        return studentRepository.save(s);
    }

    /** Delete student by ID */
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    /** Get total student count */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return studentRepository.count();
    }

    /** Get count of online students */
    @Transactional(readOnly = true)
    public long getOnlineCount() {
        return studentRepository.findByStatus("ONLINE").size();
    }

    /** Mark student as seen (last seen update) */
    public void markSeen(Long id) {
        studentRepository.findById(id).ifPresent(s -> {
            s.setLastSeen(LocalDateTime.now());
            studentRepository.save(s);
        });
    }

    /** Mark student as ONLINE by username */
    public void markOnline(String username) {
        studentRepository.findByUsername(username).ifPresent(s -> {
            s.setStatus("ONLINE");
            s.setLastSeen(LocalDateTime.now());
            studentRepository.save(s);
            log.info("[AUDIT] Student '{}' marked ONLINE", username);
        });
    }

    /** Mark student as OFFLINE by username */
    public void markOffline(String username) {
        studentRepository.findByUsername(username).ifPresent(s -> {
            s.setStatus("OFFLINE");
            s.setLastSeen(LocalDateTime.now());
            studentRepository.save(s);
            log.info("[AUDIT] Student '{}' marked OFFLINE", username);
        });
    }
}

