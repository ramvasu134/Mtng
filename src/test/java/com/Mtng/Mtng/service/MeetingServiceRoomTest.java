package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.repository.MeetingRepository;
import com.Mtng.Mtng.repository.RecordingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MeetingServiceRoomTest – tests for stable room name generation
 * and participant management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingService Room & Participant Tests")
class MeetingServiceRoomTest {

    @Mock private MeetingRepository meetingRepository;
    @Mock private RecordingRepository recordingRepository;
    @InjectMocks private MeetingService meetingService;

    @Test
    @DisplayName("startMeeting – generates a stable roomName")
    void startMeeting_generatesRoomName() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of());
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        Meeting result = meetingService.startMeeting("Room Test");

        assertThat(result.getRoomName()).isNotNull();
        assertThat(result.getRoomName()).startsWith("mtng-");
        assertThat(result.getRoomName().length()).isGreaterThan(5);
    }

    @Test
    @DisplayName("startMeeting – different calls generate different room names")
    void startMeeting_differentRoomNames() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of());
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        Meeting m1 = meetingService.startMeeting("Meeting 1");
        Meeting m2 = meetingService.startMeeting("Meeting 2");

        assertThat(m1.getRoomName()).isNotEqualTo(m2.getRoomName());
    }

    @Test
    @DisplayName("addStudentToMeeting – adds user to participant set")
    void addStudent_addsToSet() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of());
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(new Meeting()));

        Meeting m = meetingService.startMeeting("Participant Test");
        meetingService.addStudentToMeeting(1L, "HARI34");

        Set<String> participants = meetingService.getMeetingParticipants(1L);
        assertThat(participants).contains("HARI34");
    }

    @Test
    @DisplayName("removeStudentFromMeeting – removes user from participant set")
    void removeStudent_removesFromSet() {
        when(meetingRepository.findByActive(true)).thenReturn(List.of());
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(new Meeting()));

        meetingService.startMeeting("Remove Test");
        meetingService.addStudentToMeeting(1L, "HARI34");
        meetingService.addStudentToMeeting(1L, "PRIYA01");
        meetingService.removeStudentFromMeeting(1L, "HARI34");

        Set<String> participants = meetingService.getMeetingParticipants(1L);
        assertThat(participants).doesNotContain("HARI34");
        assertThat(participants).contains("PRIYA01");
    }

    @Test
    @DisplayName("getMeetingParticipants – returns empty set for unknown meeting")
    void getParticipants_unknownMeeting() {
        Set<String> participants = meetingService.getMeetingParticipants(999L);
        assertThat(participants).isEmpty();
    }

    @Test
    @DisplayName("stopMeeting – clears participants from memory")
    void stopMeeting_clearsParticipants() {
        Meeting active = new Meeting();
        active.setId(1L);
        active.setActive(true);
        active.setStartTime(LocalDateTime.now());

        when(meetingRepository.findByActive(true))
                .thenReturn(List.of())      // first call for startMeeting
                .thenReturn(List.of(active)); // second call for stopMeeting
        when(meetingRepository.save(any())).thenAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            if (m.getId() == null) m.setId(1L);
            return m;
        });
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(active));

        meetingService.startMeeting("Clear Test");
        meetingService.addStudentToMeeting(1L, "HARI34");
        meetingService.stopMeeting();

        Set<String> participants = meetingService.getMeetingParticipants(1L);
        assertThat(participants).isEmpty();
    }

    @Test
    @DisplayName("updateCounters – updates meeting in-meeting and online counts")
    void updateCounters() {
        Meeting m = new Meeting();
        m.setId(1L);
        m.setInMeetingCount(0);
        m.setOnlineCount(0);

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(m));
        when(meetingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Meeting result = meetingService.updateCounters(1L, 5, 10);
        assertThat(result.getInMeetingCount()).isEqualTo(5);
        assertThat(result.getOnlineCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("updateCounters – throws for unknown meeting")
    void updateCounters_unknownMeeting() {
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> meetingService.updateCounters(999L, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Meeting not found");
    }
}

