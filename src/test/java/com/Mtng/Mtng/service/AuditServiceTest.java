package com.Mtng.Mtng.service;

import com.Mtng.Mtng.model.AuditLog;
import com.Mtng.Mtng.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuditServiceTest – unit tests for AuditService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Tests")
class AuditServiceTest {

    @Mock private AuditLogRepository auditRepo;
    @InjectMocks private AuditService auditService;

    @Test
    @DisplayName("logEvent – creates audit log with all fields")
    void logEvent_allFields() {
        when(auditRepo.save(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        auditService.logEvent("admin", "MEETING_START", "Started meeting 'Test'", "127.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditRepo).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getActor()).isEqualTo("admin");
        assertThat(saved.getAction()).isEqualTo("MEETING_START");
        assertThat(saved.getDetails()).isEqualTo("Started meeting 'Test'");
        assertThat(saved.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(saved.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("logEvent – works without IP")
    void logEvent_withoutIp() {
        when(auditRepo.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));
        auditService.logEvent("HARI34", "JOIN", "Joined meeting");
        verify(auditRepo).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("getRecentLogs – returns last 100 logs")
    void getRecentLogs() {
        AuditLog log = new AuditLog();
        log.setActor("admin");
        log.setAction("TEST");
        when(auditRepo.findTop100ByOrderByTimestampDesc()).thenReturn(List.of(log));

        List<AuditLog> result = auditService.getRecentLogs();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActor()).isEqualTo("admin");
    }

    @Test
    @DisplayName("getLogsByActor – returns logs for specific actor")
    void getLogsByActor() {
        when(auditRepo.findByActorOrderByTimestampDesc("admin")).thenReturn(List.of(new AuditLog()));
        List<AuditLog> result = auditService.getLogsByActor("admin");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getLogsByAction – returns logs for specific action")
    void getLogsByAction() {
        when(auditRepo.findByActionOrderByTimestampDesc("LOGIN")).thenReturn(List.of(new AuditLog()));
        List<AuditLog> result = auditService.getLogsByAction("LOGIN");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getLogsBetween – returns logs in date range")
    void getLogsBetween() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(auditRepo.findByTimestampBetweenOrderByTimestampDesc(from, to)).thenReturn(List.of());
        List<AuditLog> result = auditService.getLogsBetween(from, to);
        assertThat(result).isEmpty();
    }
}

