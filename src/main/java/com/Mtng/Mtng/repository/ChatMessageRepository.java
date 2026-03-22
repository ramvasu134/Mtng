package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/** Repository for ChatMessage entity */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByMeetingIdOrderBySentAtAsc(Long meetingId);
    void deleteByMeetingId(Long meetingId);
}

