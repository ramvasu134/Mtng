package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Meeting entity – supports multiple concurrent rooms.
 *
 * <p>Index-backed queries: active, room_name, created_by are all indexed on the table.</p>
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Meeting> findByActive(boolean active);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Meeting> findByActiveTrue();

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<Meeting> findByRoomNameAndActiveTrue(String roomName);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<Meeting> findByRoomName(String roomName);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Meeting> findByCreatedByAndActiveTrue(String createdBy);
}
