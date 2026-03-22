package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.Student;
import com.Mtng.Mtng.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StudentServiceTest – unit tests for StudentService.
 *
 * <p>Uses Mockito to mock the repository layer. No Spring context needed.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
class StudentServiceTest {

    @Mock  private StudentRepository studentRepository;
    @Mock  private PasswordEncoder passwordEncoder;
    @InjectMocks private StudentService studentService;

    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        sampleStudent = new Student();
        sampleStudent.setId(1L);
        sampleStudent.setName("Hari");
        sampleStudent.setUsername("HARI34");
        sampleStudent.setPassword("pass1234");
        sampleStudent.setStatus("OFFLINE");
    }

    // ── Create ────────────────────────────────────────────────

    @Test
    @DisplayName("createStudent – success when username is unique")
    void createStudent_success() {
        when(studentRepository.existsByUsername("HARI34")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        Student result = studentService.createStudent(sampleStudent);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Hari");
        verify(studentRepository).save(sampleStudent);
    }

    @Test
    @DisplayName("createStudent – throws when username already exists")
    void createStudent_duplicateUsername_throws() {
        when(studentRepository.existsByUsername("HARI34")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(sampleStudent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");
        verify(studentRepository, never()).save(any());
    }

    // ── Find ──────────────────────────────────────────────────

    @Test
    @DisplayName("findById – returns student when found")
    void findById_found() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));
        Optional<Student> result = studentService.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById – returns empty when not found")
    void findById_notFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Student> result = studentService.findById(99L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllStudents – returns all")
    void getAllStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(sampleStudent));
        List<Student> list = studentService.getAllStudents();
        assertThat(list).hasSize(1);
    }

    // ── Block / Mute ──────────────────────────────────────────

    @Test
    @DisplayName("toggleBlock – flips blocked to true")
    void toggleBlock_blocksStudent() {
        sampleStudent.setBlocked(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Student result = studentService.toggleBlock(1L);
        assertThat(result.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("toggleBlock – flips blocked to false (unblock)")
    void toggleBlock_unblocksStudent() {
        sampleStudent.setBlocked(true);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Student result = studentService.toggleBlock(1L);
        assertThat(result.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("toggleMute – flips muted flag")
    void toggleMute() {
        sampleStudent.setMuted(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));
        when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Student result = studentService.toggleMute(1L);
        assertThat(result.isMuted()).isTrue();
    }

    // ── Delete ────────────────────────────────────────────────

    @Test
    @DisplayName("deleteStudent – calls repository deleteById")
    void deleteStudent() {
        doNothing().when(studentRepository).deleteById(1L);
        studentService.deleteStudent(1L);
        verify(studentRepository).deleteById(1L);
    }

    // ── Counts ────────────────────────────────────────────────

    @Test
    @DisplayName("getTotalCount – returns repository count")
    void getTotalCount() {
        when(studentRepository.count()).thenReturn(5L);
        assertThat(studentService.getTotalCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getOnlineCount – returns count of ONLINE students")
    void getOnlineCount() {
        Student online = new Student();
        online.setStatus("ONLINE");
        when(studentRepository.findByStatus("ONLINE")).thenReturn(List.of(online));
        assertThat(studentService.getOnlineCount()).isEqualTo(1L);
    }

    // ── Search ────────────────────────────────────────────────

    @Test
    @DisplayName("search – blank keyword returns all")
    void search_blankKeyword_returnsAll() {
        when(studentRepository.findAll()).thenReturn(List.of(sampleStudent));
        List<Student> result = studentService.search("  ");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("search – keyword delegates to repository")
    void search_withKeyword() {
        when(studentRepository.findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase("hari","hari"))
                .thenReturn(List.of(sampleStudent));
        List<Student> result = studentService.search("hari");
        assertThat(result).hasSize(1);
    }
}

