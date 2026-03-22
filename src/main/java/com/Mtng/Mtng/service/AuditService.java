package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.AuditLog;
import com.Mtng.Mtng.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditService – records and retrieves audit log entries.
 * All significant user actions are logged for accountability.
 */
@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditRepo;

    @Autowired
    public AuditService(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    /**
     * Log an auditable event.
     *
     * @param actor   Username performing the action
     * @param action  Action type (e.g. "LOGIN", "MEETING_START", "CHAT_SEND")
     * @param details Human-readable detail string
     * @param ip      Client IP address (nullable)
     */
    public void logEvent(String actor, String action, String details, String ip) {
        AuditLog entry = new AuditLog();
        entry.setActor(actor);
        entry.setAction(action);
        entry.setDetails(details);
        entry.setIpAddress(ip);
        entry.setTimestamp(LocalDateTime.now());
        auditRepo.save(entry);
        log.info("[AUDIT] actor={}, action={}, details={}, ip={}", actor, action, details, ip);
    }

    /** Convenience overload without IP */
    public void logEvent(String actor, String action, String details) {
        logEvent(actor, action, details, null);
    }

    /** Get recent audit logs (last 100) */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        return auditRepo.findTop100ByOrderByTimestampDesc();
    }

    /** Get logs for a specific actor */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByActor(String actor) {
        return auditRepo.findByActorOrderByTimestampDesc(actor);
    }

    /** Get logs by action type */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByAction(String action) {
        return auditRepo.findByActionOrderByTimestampDesc(action);
    }

    /** Get logs in a date range */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsBetween(LocalDateTime from, LocalDateTime to) {
        return auditRepo.findByTimestampBetweenOrderByTimestampDesc(from, to);
    }
}

