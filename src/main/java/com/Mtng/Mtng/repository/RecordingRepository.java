package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/** Repository for Recording entity */
@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<Recording> findByStudentIdOrderByRecordingDateDescRecordingTimeDesc(Long studentId);
    List<Recording> findByMeetingId(Long meetingId);
    void deleteByStudentId(Long studentId);
}

