package com.Mtng.Mtng.service;

import com.Mtng.Mtng.repository.StudentRepository;
import com.Mtng.Mtng.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * MtngUserDetailsService – bridges Spring Security authentication
 * with both the Teacher table (ADMIN/USER) and the Student table (ROLE_USER).
 */
@Service
public class MtngUserDetailsService implements UserDetailsService {

    private final TeacherRepository teacherRepo;
    private final StudentRepository studentRepo;

    @Autowired
    public MtngUserDetailsService(TeacherRepository teacherRepo, StudentRepository studentRepo) {
        this.teacherRepo = teacherRepo;
        this.studentRepo = studentRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Check Teacher table first (admin & user accounts)
        var teacher = teacherRepo.findByUsername(username);
        if (teacher.isPresent()) {
            var t = teacher.get();
            return User.builder()
                    .username(t.getUsername())
                    .password(t.getPassword())   // BCrypt-encoded
                    .roles(t.getRole().replace("ROLE_", ""))
                    .build();
        }

        // 2. Fall back to Student table – students log in as ROLE_USER
        return studentRepo.findByUsername(username)
                .map(s -> User.builder()
                        .username(s.getUsername())
                        .password(s.getPassword())   // BCrypt-encoded (set on create)
                        .disabled(s.isBlocked())     // blocked students cannot log in
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No teacher or student found with username: " + username));
    }
}
