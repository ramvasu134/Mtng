package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.repository.MeetingRepository;
import com.Mtng.Mtng.repository.RecordingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MeetingServiceTest – unit tests for MeetingService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingService Tests")
class MeetingServiceTest {

    @Mock  private MeetingRepository meetingRepository;
    @Mock  private RecordingRepository recordingRepository;
    @InjectMocks private MeetingService meetingService;

    private Meeting activeMeeting;

    @BeforeEach
    void setUp() {
        activeMeeting = new Meeting();
        activeMeeting.setId(1L);
        activeMeeting.setTitle("Test Session");
        activeMeeting.setActive(true);
        activeMeeting.setStartTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("startMeeting – creates active meeting")
    void startMeeting_createsActiveMeeting() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of());
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            m.setId(2L);
            return m;
        });

        Meeting result = meetingService.startMeeting("Mtng Session");

        assertThat(result.isActive()).isTrue();
        assertThat(result.getTitle()).isEqualTo("Mtng Session");
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    @DisplayName("startMeeting – stops existing active meeting first")
    void startMeeting_stopsExistingFirst() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of(activeMeeting));
        when(meetingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        meetingService.startMeeting("New Session");

        // Existing meeting should be deactivated
        assertThat(activeMeeting.isActive()).isFalse();
    }

    @Test
    @DisplayName("stopMeeting – sets active to false and sets endTime")
    void stopMeeting() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of(activeMeeting));
        when(meetingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        meetingService.stopMeeting();

        assertThat(activeMeeting.isActive()).isFalse();
        assertThat(activeMeeting.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("getActiveMeeting – returns present when active exists")
    void getActiveMeeting_present() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of(activeMeeting));
        Optional<Meeting> result = meetingService.getActiveMeeting();
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getActiveMeeting – returns empty when none active")
    void getActiveMeeting_empty() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of());
        Optional<Meeting> result = meetingService.getActiveMeeting();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toggleFullRecording – flips fullRecording flag")
    void toggleFullRecording() {
        activeMeeting.setFullRecording(false);
        when(meetingRepository.findByActive(true)).thenReturn(List.of(activeMeeting));
        when(meetingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Meeting result = meetingService.toggleFullRecording();
        assertThat(result.isFullRecording()).isTrue();
    }

    @Test
    @DisplayName("toggleFullRecording – throws when no active meeting")
    void toggleFullRecording_noActiveMeeting_throws() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of());
        assertThatThrownBy(() -> meetingService.toggleFullRecording())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No active meeting");
    }

    @Test
    @DisplayName("getAllMeetings – delegates to repository")
    void getAllMeetings() {
        when(meetingRepository.findAll()).thenReturn(List.of(activeMeeting));
        List<Meeting> result = meetingService.getAllMeetings();
        assertThat(result).hasSize(1);
    }
}

