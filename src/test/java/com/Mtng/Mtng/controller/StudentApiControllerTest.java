package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.Student;
import com.Mtng.Mtng.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StudentApiControllerTest – pure Mockito unit tests for StudentApiController.
 * Tests controller logic without starting a Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentApiController Tests")
class StudentApiControllerTest {

    @Mock  private StudentService studentService;
    @InjectMocks private StudentApiController controller;

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

    @Test
    @DisplayName("list – returns all students")
    void list_returnsAll() {
        when(studentService.getAllStudents()).thenReturn(List.of(sampleStudent));
        List<Student> result = controller.list();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hari");
    }

    @Test
    @DisplayName("get – returns 200 when found")
    void get_found() {
        when(studentService.findById(1L)).thenReturn(Optional.of(sampleStudent));
        ResponseEntity<Student> resp = controller.get(1L);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
    }

    @Test
    @DisplayName("get – returns 404 when not found")
    void get_notFound() {
        when(studentService.findById(99L)).thenReturn(Optional.empty());
        ResponseEntity<Student> resp = controller.get(99L);
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("create – returns 200 on success")
    void create_success() {
        when(studentService.createStudent(any(Student.class))).thenReturn(sampleStudent);
        ResponseEntity<?> resp = controller.create(sampleStudent);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("create – returns 400 on duplicate username")
    void create_duplicate() {
        when(studentService.createStudent(any()))
                .thenThrow(new IllegalArgumentException("Username already exists: HARI34"));
        ResponseEntity<?> resp = controller.create(sampleStudent);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        @SuppressWarnings("unchecked")
        Map<String,String> body = (Map<String,String>) resp.getBody();
        assertThat(body).containsKey("error");
    }

    @Test
    @DisplayName("delete – returns 204")
    void delete_returns204() {
        doNothing().when(studentService).deleteStudent(1L);
        ResponseEntity<Void> resp = controller.delete(1L);
        assertThat(resp.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    @DisplayName("block – returns blocked student")
    void block_returnsBlockedStudent() {
        sampleStudent.setBlocked(true);
        when(studentService.toggleBlock(1L)).thenReturn(sampleStudent);
        ResponseEntity<Student> resp = controller.block(1L);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().isBlocked()).isTrue();
    }

    @Test
    @DisplayName("mute – returns muted student")
    void mute_returnsMutedStudent() {
        sampleStudent.setMuted(true);
        when(studentService.toggleMute(1L)).thenReturn(sampleStudent);
        ResponseEntity<Student> resp = controller.mute(1L);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().isMuted()).isTrue();
    }

    @Test
    @DisplayName("update – returns 200 on success")
    void update_success() {
        when(studentService.updateStudent(eq(1L), any(Student.class))).thenReturn(sampleStudent);
        ResponseEntity<?> resp = controller.update(1L, sampleStudent);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }
}
