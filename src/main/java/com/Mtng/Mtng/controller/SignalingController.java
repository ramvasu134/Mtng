package com.Mtng.Mtng.controller;

import com.Mtng.Mtng.model.SignalMessage;
import com.Mtng.Mtng.model.Student;
import com.Mtng.Mtng.model.Teacher;
import com.Mtng.Mtng.repository.StudentRepository;
import com.Mtng.Mtng.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Optional;

/**
 * SignalingController – routes WebRTC signaling messages between peers.
 *
 * <p>All messages sent to /app/signal/{roomId} are broadcast to all
 * subscribers of /topic/room/{roomId}. Each client filters by the
 * "to" field (null = intended for all, or a specific username).</p>
 *
 * <p>This completely replaces Jitsi's public server – no external
 * authentication or third-party dependency required.</p>
 */
@Controller
public class SignalingController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Autowired
    public SignalingController(StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    /**
     * Handle a WebRTC signaling message and broadcast it to all room participants.
     *
     * @param roomId   the meeting room identifier (e.g. "mtng-abc12345")
     * @param message  the signaling message (join/offer/answer/ice-candidate/leave)
     * @param principal the authenticated user (auto-injected by Spring Security + WebSocket)
     * @return the message broadcast to /topic/room/{roomId}
     */
    @MessageMapping("/signal/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public SignalMessage signal(@DestinationVariable String roomId,
                                SignalMessage message,
                                Principal principal) {
        String username = principal != null ? principal.getName() : null;
        
        // Normalize: support both 'sender' and 'from' from frontend
        if (username != null && (message.getFrom() == null || message.getFrom().isBlank())) {
            message.setFrom(username);
        }
        
        // Support both 'target' and 'to' fields
        if (message.getTarget() != null && message.getTo() == null) {
            message.setTo(message.getTarget());
        }
        
        // Add displayName if not provided
        if ((message.getDisplayName() == null || message.getDisplayName().isBlank()) && username != null) {
            String displayName = fetchDisplayName(username);
            message.setDisplayName(displayName);
        }
        
        return message;
    }

    /**
     * Fetch displayName from Student or Teacher record
     */
    private String fetchDisplayName(String username) {
        // Try to find in Student table
        Optional<Student> student = studentRepository.findByUsername(username);
        if (student.isPresent()) {
            // Student has 'name' field, no displayName
            return student.get().getName() != null ? 
                   student.get().getName() : 
                   username;
        }
        
        // Try to find in Teacher table
        Optional<Teacher> teacher = teacherRepository.findByUsername(username);
        if (teacher.isPresent()) {
            // Teacher also has 'name' field
            return teacher.get().getName() != null ? 
                   teacher.get().getName() : 
                   username;
        }
        
        // Fallback to username
        return username;
    }
}

