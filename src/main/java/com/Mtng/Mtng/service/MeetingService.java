package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.Meeting;
import com.Mtng.Mtng.model.Recording;
import com.Mtng.Mtng.repository.MeetingRepository;
import com.Mtng.Mtng.repository.RecordingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * MeetingService – business logic for meeting lifecycle management.
 * Start/stop meeting, track participants, save recordings.
 */
@Service
@Transactional
public class MeetingService {

    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

    private final MeetingRepository meetingRepository;
    private final RecordingRepository recordingRepository;
    private final Map<Long, Set<String>> meetingParticipants = new HashMap<>();

    @Autowired
    public MeetingService(MeetingRepository meetingRepository, RecordingRepository recordingRepository) {
        this.meetingRepository = meetingRepository;
        this.recordingRepository = recordingRepository;
    }

    /** Get current active meeting or empty */
    @Transactional(readOnly = true)
    public Optional<Meeting> getActiveMeeting() {
        List<Meeting> active = meetingRepository.findByActive(true);
        return active.isEmpty() ? Optional.empty() : Optional.of(active.get(0));
    }

    /** Start a new meeting with a stable Jitsi room name */
    public Meeting startMeeting(String title) {
        log.info("Starting new meeting: title={}", title);
        // stop any running meeting first
        meetingRepository.findByActive(true).forEach(m -> {
            log.info("Stopping existing active meeting: id={}, title={}", m.getId(), m.getTitle());
            m.setActive(false);
            m.setEndTime(java.time.LocalDateTime.now());
            meetingRepository.save(m);
        });
        Meeting m = new Meeting();
        m.setTitle(title);
        m.setActive(true);
        m.setStartTime(java.time.LocalDateTime.now());
        // Generate a STABLE room name that all participants will share
        String roomName = "mtng-" + UUID.randomUUID().toString().substring(0, 8);
        m.setRoomName(roomName);
        m = meetingRepository.save(m);
        meetingParticipants.put(m.getId(), new HashSet<>());
        log.info("Meeting started: id={}, roomName={}", m.getId(), roomName);
        return m;
    }

    /** Stop the currently active meeting and save recordings */
    public void stopMeeting() {
        List<Meeting> active = meetingRepository.findByActive(true);
        if (active.isEmpty()) {
            log.warn("stopMeeting called but no active meeting found");
            return;
        }

        Meeting m = active.get(0);
        log.info("Stopping meeting: id={}, title={}", m.getId(), m.getTitle());
        m.setActive(false);
        m.setEndTime(java.time.LocalDateTime.now());

        if (m.isFullRecording() && meetingParticipants.containsKey(m.getId())) {
            Set<String> participants = meetingParticipants.get(m.getId());
            log.info("Saving recordings for {} participants", participants.size());
            participants.forEach(username -> {
                Recording rec = new Recording();
                rec.setMeetingId(m.getId());
                rec.setStudentName(username);
                rec.setStudentId(1L);
                rec.setRecordingDate(LocalDate.now());
                rec.setRecordingTime(LocalTime.now());
                rec.setDurationSeconds(calculateDuration(m.getStartTime(), m.getEndTime()));
                recordingRepository.save(rec);
                log.debug("Recording saved for user: {}", username);
            });
        }

        meetingParticipants.remove(m.getId());
        meetingRepository.save(m);
        log.info("Meeting stopped: id={}", m.getId());
    }

    /** Add student to meeting */
    public void addStudentToMeeting(Long meetingId, String username) {
        if (!meetingParticipants.containsKey(meetingId)) {
            meetingParticipants.put(meetingId, new HashSet<>());
        }
        meetingParticipants.get(meetingId).add(username);
        updateInMeetingCount(meetingId);
        log.info("User '{}' joined meeting {}", username, meetingId);
    }

    /** Remove student from meeting */
    public void removeStudentFromMeeting(Long meetingId, String username) {
        if (meetingParticipants.containsKey(meetingId)) {
            meetingParticipants.get(meetingId).remove(username);
            updateInMeetingCount(meetingId);
            log.info("User '{}' left meeting {}", username, meetingId);
        }
    }

    /** Update in-meeting count */
    private void updateInMeetingCount(Long meetingId) {
        Optional<Meeting> m = meetingRepository.findById(meetingId);
        if (m.isPresent()) {
            int count = meetingParticipants.getOrDefault(meetingId, new HashSet<>()).size();
            m.get().setInMeetingCount(count);
            meetingRepository.save(m.get());
        }
    }

    /** Get participants in meeting */
    public Set<String> getMeetingParticipants(Long meetingId) {
        return meetingParticipants.getOrDefault(meetingId, new HashSet<>());
    }

    /** Toggle full recording on active meeting */
    public Meeting toggleFullRecording() {
        Meeting m = getActiveMeeting()
                .orElseThrow(() -> new IllegalStateException("No active meeting"));
        m.setFullRecording(!m.isFullRecording());
        log.info("Recording toggled to {} for meeting {}", m.isFullRecording(), m.getId());
        return meetingRepository.save(m);
    }

    /** Update participant counters */
    public Meeting updateCounters(Long id, int inMeeting, int online) {
        Meeting m = meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + id));
        m.setInMeetingCount(inMeeting);
        m.setOnlineCount(online);
        return meetingRepository.save(m);
    }

    /** List all meetings */
    @Transactional(readOnly = true)
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    /** Find by ID */
    @Transactional(readOnly = true)
    public Optional<Meeting> findById(Long id) {
        return meetingRepository.findById(id);
    }

    /** Calculate duration in seconds */
    private int calculateDuration(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (start == null || end == null) return 0;
        return (int) java.time.temporal.ChronoUnit.SECONDS.between(start, end);
    }
}

