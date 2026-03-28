package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity.
 *
 * <p>Performance notes:</p>
 * <ul>
 *   <li>All read queries use @QueryHints(readOnly=true) – audit logs are never modified after creation.</li>
 *   <li>Pageable variants prevent OOM on large audit tables.</li>
 *   <li>deleteOlderThan() bulk-deletes in a single DML statement for data-retention archiving.</li>
 * </ul>
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<AuditLog> findByActorOrderByTimestampDesc(String actor);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<AuditLog> findByActorOrderByTimestampDesc(String actor, Pageable pageable);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<AuditLog> findTop100ByOrderByTimestampDesc();

    /** Pageable version – use this for dashboard/admin views to avoid full-table load. */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Data-retention archiving: bulk-delete audit logs older than the given cutoff.
     * Call periodically (e.g. monthly) to keep the production table lean.
     * Example: deleteOlderThan(LocalDateTime.now().minusDays(90))
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoff")
    int deleteOlderThan(LocalDateTime cutoff);
}
