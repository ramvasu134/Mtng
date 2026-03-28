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
import java.util.stream.Collectors;

/**
 * MeetingService – business logic for multi-room meeting lifecycle.
 * Supports multiple concurrent rooms, each with their own participants.
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

    /** Get current active meeting or empty (backward compat – returns first active) */
    @Transactional(readOnly = true)
    public Optional<Meeting> getActiveMeeting() {
        List<Meeting> active = meetingRepository.findByActiveTrue();
        return active.isEmpty() ? Optional.empty() : Optional.of(active.get(0));
    }

    /** Get ALL active meetings/rooms */
    @Transactional(readOnly = true)
    public List<Meeting> getActiveMeetings() {
        return meetingRepository.findByActiveTrue();
    }

    /** Get a specific room by roomName */
    @Transactional(readOnly = true)
    public Optional<Meeting> getMeetingByRoomName(String roomName) {
        return meetingRepository.findByRoomNameAndActiveTrue(roomName);
    }

    /** Get rooms created by a specific admin */
    @Transactional(readOnly = true)
    public List<Meeting> getRoomsByCreator(String createdBy) {
        return meetingRepository.findByCreatedByAndActiveTrue(createdBy);
    }

    /**
     * Start a new meeting room (allows multiple concurrent rooms).
     * Does NOT stop existing meetings.
     */
    public Meeting startMeeting(String title) {
        return startRoom(title, null, null);
    }

    /**
     * Start a new room with optional invited participants and creator.
     */
    public Meeting startRoom(String title, List<String> participants, String createdBy) {
        log.info("Starting new room: title={}, createdBy={}, participants={}", title, createdBy, participants);

        Meeting m = new Meeting();
        m.setTitle(title);
        m.setActive(true);
        m.setStartTime(java.time.LocalDateTime.now());
        m.setCreatedBy(createdBy);

        // Generate a stable room name
        String roomName = "mtng-" + UUID.randomUUID().toString().substring(0, 8);
        m.setRoomName(roomName);

        // Store invited participants as a normalised Set (1NF – no comma lists)
        if (participants != null && !participants.isEmpty()) {
            m.setInvitedParticipants(new HashSet<>(participants));
        }

        m = meetingRepository.save(m);
        meetingParticipants.put(m.getId(), new HashSet<>());
        log.info("Room started: id={}, roomName={}, participants={}", m.getId(), roomName, participants);
        return m;
    }

    /** Stop a specific meeting by ID */
    public void stopMeetingById(Long meetingId) {
        Optional<Meeting> opt = meetingRepository.findById(meetingId);
        if (opt.isEmpty()) {
            log.warn("stopMeetingById called but meeting not found: {}", meetingId);
            return;
        }
        Meeting m = opt.get();
        stopSingleMeeting(m);
    }

    /** Stop a specific meeting by room name */
    public void stopMeetingByRoomName(String roomName) {
        Optional<Meeting> opt = meetingRepository.findByRoomNameAndActiveTrue(roomName);
        if (opt.isEmpty()) {
            log.warn("stopMeetingByRoomName called but no active room found: {}", roomName);
            return;
        }
        stopSingleMeeting(opt.get());
    }

    /** Stop the first active meeting (backward compat) */
    public void stopMeeting() {
        List<Meeting> active = meetingRepository.findByActiveTrue();
        if (active.isEmpty()) {
            log.warn("stopMeeting called but no active meeting found");
            return;
        }
        stopSingleMeeting(active.get(0));
    }

    /** Stop all active meetings */
    public void stopAllMeetings() {
        List<Meeting> active = meetingRepository.findByActiveTrue();
        for (Meeting m : active) {
            stopSingleMeeting(m);
        }
    }

    private void stopSingleMeeting(Meeting m) {
        log.info("Stopping room: id={}, title={}", m.getId(), m.getTitle());
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
            });
        }

        meetingParticipants.remove(m.getId());
        meetingRepository.save(m);
        log.info("Room stopped: id={}", m.getId());
    }

    /** Add student to meeting */
    public void addStudentToMeeting(Long meetingId, String username) {
        if (!meetingParticipants.containsKey(meetingId)) {
            meetingParticipants.put(meetingId, new HashSet<>());
        }
        meetingParticipants.get(meetingId).add(username);
        updateInMeetingCount(meetingId);
        log.info("User '{}' joined room {}", username, meetingId);
    }

    /** Remove student from meeting */
    public void removeStudentFromMeeting(Long meetingId, String username) {
        if (meetingParticipants.containsKey(meetingId)) {
            meetingParticipants.get(meetingId).remove(username);
            updateInMeetingCount(meetingId);
            log.info("User '{}' left room {}", username, meetingId);
        }
    }

    /** Update in-meeting count */
    private void updateInMeetingCount(Long meetingId) {
        meetingRepository.findById(meetingId).ifPresent(m -> {
            int count = meetingParticipants.getOrDefault(meetingId, new HashSet<>()).size();
            m.setInMeetingCount(count);
            meetingRepository.save(m);
        });
    }

    /** Get participants in meeting */
    public Set<String> getMeetingParticipants(Long meetingId) {
        return meetingParticipants.getOrDefault(meetingId, new HashSet<>());
    }

    /** Toggle full recording on a specific meeting */
    public Meeting toggleFullRecording(Long meetingId) {
        Meeting m = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalStateException("Meeting not found: " + meetingId));
        m.setFullRecording(!m.isFullRecording());
        log.info("Recording toggled to {} for room {}", m.isFullRecording(), m.getId());
        return meetingRepository.save(m);
    }

    /** Toggle full recording on first active meeting (backward compat) */
    public Meeting toggleFullRecording() {
        Meeting m = getActiveMeeting()
                .orElseThrow(() -> new IllegalStateException("No active meeting"));
        m.setFullRecording(!m.isFullRecording());
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

