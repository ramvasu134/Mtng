package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.Recording;
import com.Mtng.Mtng.repository.RecordingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RecordingServiceTest – unit tests for RecordingService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecordingService Tests")
class RecordingServiceTest {

    @Mock  private RecordingRepository recordingRepo;
    @InjectMocks private RecordingService recordingService;

    @Test
    @DisplayName("addRecording – saves and returns recording")
    void addRecording_saves() {
        Recording saved = new Recording();
        saved.setId(1L);
        saved.setStudentId(1L);
        saved.setStudentName("Hari");
        saved.setDurationSeconds(6);

        when(recordingRepo.save(any(Recording.class))).thenReturn(saved);

        Recording result = recordingService.addRecording(1L, "Hari", 6, 1L);
        assertThat(result.getStudentName()).isEqualTo("Hari");
        assertThat(result.getDurationSeconds()).isEqualTo(6);
        verify(recordingRepo).save(any(Recording.class));
    }

    @Test
    @DisplayName("getAllRecordings – returns all")
    void getAllRecordings() {
        when(recordingRepo.findAll()).thenReturn(List.of(new Recording()));
        List<Recording> result = recordingService.getAllRecordings();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getStudentRecordings – returns by studentId")
    void getStudentRecordings() {
        Recording r = new Recording();
        r.setStudentId(1L);
        when(recordingRepo.findByStudentIdOrderByRecordingDateDescRecordingTimeDesc(1L))
                .thenReturn(List.of(r));
        List<Recording> result = recordingService.getStudentRecordings(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("deleteRecording – calls deleteById")
    void deleteRecording() {
        doNothing().when(recordingRepo).deleteById(1L);
        recordingService.deleteRecording(1L);
        verify(recordingRepo).deleteById(1L);
    }

    @Test
    @DisplayName("clearAll – calls deleteAll")
    void clearAll() {
        doNothing().when(recordingRepo).deleteAll();
        recordingService.clearAll();
        verify(recordingRepo).deleteAll();
    }

    @Test
    @DisplayName("deleteStudentRecordings – calls deleteByStudentId")
    void deleteStudentRecordings() {
        doNothing().when(recordingRepo).deleteByStudentId(1L);
        recordingService.deleteStudentRecordings(1L);
        verify(recordingRepo).deleteByStudentId(1L);
    }
}

