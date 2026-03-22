package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.Recording;
import com.Mtng.Mtng.repository.RecordingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    /** All recordings for a student */
    @Transactional(readOnly = true)
    public List<Recording> getStudentRecordings(Long studentId) {
        return recordingRepo.findByStudentIdOrderByRecordingDateDescRecordingTimeDesc(studentId);
    }

    /** All recordings */
    @Transactional(readOnly = true)
    public List<Recording> getAllRecordings() {
        return recordingRepo.findAll();
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

