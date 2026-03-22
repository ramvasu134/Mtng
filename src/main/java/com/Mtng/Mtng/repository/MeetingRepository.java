package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/** Repository for Meeting entity */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByActive(boolean active);
}
