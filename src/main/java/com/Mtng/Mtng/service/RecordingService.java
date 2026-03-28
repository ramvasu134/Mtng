package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.Recording;
import com.Mtng.Mtng.repository.RecordingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * RecordingService – manages student recordings.
 */
@Service
@Transactional
public class RecordingService {

    private final RecordingRepository recordingRepo;

    @Autowired
    public RecordingService(RecordingRepository recordingRepo) {
        this.recordingRepo = recordingRepo;
    }

    /** Create a recording entry */
    public Recording addRecording(Long studentId, String studentName, int durationSeconds, Long meetingId) {
        Recording r = new Recording();
        r.setStudentId(studentId);
        r.setStudentName(studentName);
        r.setDurationSeconds(durationSeconds);
        r.setMeetingId(meetingId);
        r.setRecordingDate(LocalDate.now());
        r.setRecordingTime(LocalTime.now().withNano(0));
        return recordingRepo.save(r);
    }

    /**
     * Save recording session metadata from client-side MediaRecorder.
     * The actual audio file is downloaded to the user's local disk;
     * this entry records the session info in the database.
     */
    public Recording saveSession(Long meetingId, String participantName, Long studentId,
                                  int durationSeconds, String fileName,
                                  Long fileSizeBytes, String participantType) {
        Recording r = new Recording();
        r.setMeetingId(meetingId);
        r.setStudentName(participantName);
        r.setStudentId(studentId != null ? studentId : 1L);
        r.setDurationSeconds(durationSeconds);
        r.setFileName(fileName);
        r.setFileSizeBytes(fileSizeBytes != null ? fileSizeBytes : 0L);
        r.setParticipantType(participantType != null ? participantType : "PARTICIPANT");
        r.setSavedToDatabase(true);
        r.setRecordingDate(LocalDate.now());
        r.setRecordingTime(LocalTime.now().withNano(0));
        return recordingRepo.save(r);
    }

    /**
     * Attach actual audio bytes to an existing recording entry.
     * Called after save-session returns the recording ID.
     */
    public void storeAudio(Long id, byte[] data, String mimeType) {
        Recording r = recordingRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found: " + id));
        r.setAudioData(data);
        r.setAudioMimeType(mimeType != null && !mimeType.isBlank() ? mimeType : "audio/webm");
        if (data != null) {
            r.setFileSizeBytes((long) data.length);
            r.setSavedToDatabase(true);
        }
        recordingRepo.save(r);
    }

    /** Get a recording by ID */
    @Transactional(readOnly = true)
    public Optional<Recording> findById(Long id) {
        return recordingRepo.findById(id);
    }

    /** All recordings for a student */
    @Transactional(readOnly = true)
    public List<Recording> getStudentRecordings(Long studentId) {
        return recordingRepo.findByStudentIdOrderByRecordingDateDescRecordingTimeDesc(studentId);
    }

    /** All recordings ordered newest-first */
    @Transactional(readOnly = true)
    public List<Recording> getAllRecordings() {
        return recordingRepo.findAllOrderByDateDesc();
    }

    /** Recordings for a specific participant name, ordered newest-first */
    @Transactional(readOnly = true)
    public List<Recording> getRecordingsByParticipantName(String name) {
        return recordingRepo.findByStudentNameOrderByDateDesc(name);
    }

    /** Delete a single recording */
    public void deleteRecording(Long id) {
        recordingRepo.deleteById(id);
    }

    /** Delete all recordings for a student */
    public void deleteStudentRecordings(Long studentId) {
        recordingRepo.deleteByStudentId(studentId);
    }

    /** Delete all recordings */
    public void clearAll() {
        recordingRepo.deleteAll();
    }
}
