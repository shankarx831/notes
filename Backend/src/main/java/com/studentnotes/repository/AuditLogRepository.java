package com.studentnotes.repository;

import com.studentnotes.model.AuditLog;
import com.studentnotes.model.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // AuditLog is append-only - no update or delete methods should be used!

    // ==================== Correlation ID queries ====================

    List<AuditLog> findByCorrelationId(String correlationId);

    // ==================== Actor-based queries ====================

    Page<AuditLog> findByActorId(Long actorId, Pageable pageable);

    Page<AuditLog> findByActorEmail(String actorEmail, Pageable pageable);

    // ==================== Action-based queries ====================

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.action IN :actions ORDER BY al.timestamp DESC")
    Page<AuditLog> findByActionIn(@Param("actions") List<AuditAction> actions, Pageable pageable);

    // ==================== Target-based queries ====================

    @Query("SELECT al FROM AuditLog al WHERE al.targetType = :targetType AND al.targetId = :targetId ORDER BY al.timestamp DESC")
    Page<AuditLog> findByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId,
            Pageable pageable);

    // ==================== Date range queries ====================

    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :fromDate AND :toDate ORDER BY al.timestamp DESC")
    Page<AuditLog> findByDateRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    // ==================== Combined filter queries ====================

    @Query("SELECT al FROM AuditLog al WHERE " +
            "(:actorId IS NULL OR al.actorId = :actorId) AND " +
            "(:action IS NULL OR al.action = :action) AND " +
            "(:targetType IS NULL OR al.targetType = :targetType) AND " +
            "(:fromDate IS NULL OR al.timestamp >= :fromDate) AND " +
            "(:toDate IS NULL OR al.timestamp <= :toDate) " +
            "ORDER BY al.timestamp DESC")
    Page<AuditLog> findByFilters(
            @Param("actorId") Long actorId,
            @Param("action") AuditAction action,
            @Param("targetType") String targetType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    // ==================== Recent activity queries ====================

    @Query("SELECT al FROM AuditLog al ORDER BY al.timestamp DESC")
    Page<AuditLog> findRecent(Pageable pageable);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.action = :action AND al.timestamp >= :since")
    long countByActionSince(@Param("action") AuditAction action, @Param("since") LocalDateTime since);
}
