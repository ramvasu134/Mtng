package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/** Repository for Student entity */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUsername(String username);
    List<Student> findByStatus(String status);
    List<Student> findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(String name, String username);
    boolean existsByUsername(String username);
}

