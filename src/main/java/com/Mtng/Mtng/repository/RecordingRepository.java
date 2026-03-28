package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/** Repository for Recording entity */
@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<Recording> findByStudentIdOrderByRecordingDateDescRecordingTimeDesc(Long studentId);
    List<Recording> findByMeetingId(Long meetingId);
    void deleteByStudentId(Long studentId);

    /** All recordings ordered newest-first (no blob data loaded – query projection) */
    @Query("SELECT r FROM Recording r ORDER BY r.recordingDate DESC, r.recordingTime DESC")
    List<Recording> findAllOrderByDateDesc();

    /** Recordings for a specific participant name, ordered newest-first */
    @Query("SELECT r FROM Recording r WHERE r.studentName = :name ORDER BY r.recordingDate DESC, r.recordingTime DESC")
    List<Recording> findByStudentNameOrderByDateDesc(String name);
}

