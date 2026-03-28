package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import jakarta.persistence.QueryHint;
import java.util.List;

/**
 * Repository for Recording entity.
 *
 * <p>Performance notes:</p>
 * <ul>
 *   <li>All list queries exclude the BLOB audioData column to avoid loading large blobs into memory.
 *       Audio is streamed separately via GET /api/recordings/{id}/audio.</li>
 *   <li>student_id, meeting_id, recording_date are indexed – covering the ORDER BY / WHERE clauses.</li>
 * </ul>
 */
@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Recording> findByStudentIdOrderByRecordingDateDescRecordingTimeDesc(Long studentId);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Recording> findByMeetingId(Long meetingId);

    void deleteByStudentId(Long studentId);

    /**
     * All recordings ordered newest-first.
     * SELECT projection deliberately excludes the BLOB column (audioData) –
     * this avoids loading potentially hundreds of MB into the JVM heap on list calls.
     */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT r FROM Recording r ORDER BY r.recordingDate DESC, r.recordingTime DESC")
    List<Recording> findAllOrderByDateDesc();

    /** Recordings for a specific participant name, ordered newest-first. */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT r FROM Recording r WHERE r.studentName = :name ORDER BY r.recordingDate DESC, r.recordingTime DESC")
    List<Recording> findByStudentNameOrderByDateDesc(String name);

    /**
     * Count recordings for a specific student – avoids loading blobs for counting.
     */
    long countByStudentId(Long studentId);
}
