package com.Mtng.Mtng.config;

import com.Mtng.Mtng.model.Recording;
import com.Mtng.Mtng.model.Student;
import com.Mtng.Mtng.model.Teacher;
import com.Mtng.Mtng.repository.RecordingRepository;
import com.Mtng.Mtng.repository.StudentRepository;
import com.Mtng.Mtng.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DataInitializer – seeds sample data into H2 on startup.
 *
 * <p>Default admin login: username=admin / password=admin123</p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final StudentRepository   studentRepo;
    private final RecordingRepository recordingRepo;
    private final TeacherRepository   teacherRepo;
    private final PasswordEncoder     passwordEncoder;

    @Autowired
    public DataInitializer(StudentRepository studentRepo,
                           RecordingRepository recordingRepo,
                           TeacherRepository teacherRepo,
                           PasswordEncoder passwordEncoder,
                           com.Mtng.Mtng.service.StudentService studentService) {
        this.studentRepo     = studentRepo;
        this.recordingRepo   = recordingRepo;
        this.teacherRepo     = teacherRepo;
        this.passwordEncoder = passwordEncoder;
        this.studentService  = studentService;
    }

    private final com.Mtng.Mtng.service.StudentService studentService;

    @Override
    public void run(String... args) {

        // Reset all students to OFFLINE on startup (they need to login again to be ONLINE)
        try { studentService.resetAllToOffline(); } catch (Exception e) { log.debug("Reset offline: {}", e.getMessage()); }

        // ── Seed admin teacher ──────────────────────────────────────────────
        if (teacherRepo.count() == 0) {
            Teacher admin = new Teacher();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setDisplayName("Admin Teacher");
            admin.setRole("ROLE_ADMIN");
            teacherRepo.save(admin);

            Teacher user = new Teacher();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setDisplayName("Normal User");
            user.setRole("ROLE_USER");
            teacherRepo.save(user);

            log.info("Teachers seeded:");
            log.info("  ADMIN  →  username: admin  /  password: admin123");
            log.info("  USER   →  username: user   /  password: user123");
        }

        // ── Seed sample students ────────────────────────────────────────────
        if (studentRepo.count() > 0) {
            log.info("Sample student data already exists – skipping seed.");
            return;
        }

        log.info("Seeding sample students ...");

        Student hari = new Student();
        hari.setName("Hari");
        hari.setUsername("HARI34");
        hari.setRawPassword("pass1234");
        hari.setPassword(passwordEncoder.encode("pass1234"));
        hari.setShowRecordings(true);
        hari.setStatus("OFFLINE");
        hari = studentRepo.save(hari);

        Student priya = new Student();
        priya.setName("Priya");
        priya.setUsername("PRIYA01");
        priya.setRawPassword("pass1234");
        priya.setPassword(passwordEncoder.encode("pass1234"));
        priya.setShowRecordings(true);
        priya.setStatus("OFFLINE");
        studentRepo.save(priya);

        Student ram = new Student();
        ram.setName("Ram");
        ram.setUsername("RAM22");
        ram.setRawPassword("pass1234");
        ram.setPassword(passwordEncoder.encode("pass1234"));
        ram.setShowRecordings(false);
        ram.setStatus("OFFLINE");
        studentRepo.save(ram);

        for (int i = 1; i <= 4; i++) {
            Recording r = new Recording();
            r.setStudentId(hari.getId());
            r.setStudentName(hari.getName());
            r.setDurationSeconds(6);
            r.setMeetingId(1L);
            r.setRecordingDate(LocalDate.of(2026, 3, 21));
            r.setRecordingTime(LocalTime.of(20, 40 + i, 0));
            recordingRepo.save(r);
        }

        log.info("Sample data seeded: {} students", studentRepo.count());
    }
}
