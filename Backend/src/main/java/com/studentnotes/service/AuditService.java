package com.studentnotes.service;

import com.studentnotes.config.CorrelationIdFilter;
import com.studentnotes.model.AuditLog;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.AuditAction;
import com.studentnotes.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Service for creating and querying audit logs.
 * Audit logs are append-only and never modified or deleted.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Logs an action synchronously within a nested independent transaction.
     * <p>
     * <strong>Reliability Invariant:</strong> This method uses
     * {@code Propagation.REQUIRES_NEW} to ensure
     * that audit entries are persisted even if the calling business transaction
     * (e.g., a Note upload)
     * fails or is rolled back. This is critical for forensic tracking of failed
     * attempts.
     * </p>
     * 
     * @param action      The specific domain action being captured.
     * @param actor       The authenticated user performing the action.
     * @param targetType  The string literal for the entity type (e.g., "Note").
     * @param targetId    The database ID of the subject entity.
     * @param description A human-readable summary of the event.
     * @return The saved AuditLog entry.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(AuditAction action, User actor, String targetType, Long targetId, String description) {
        return logAction(action, actor, targetType, targetId, description, null, null);
    }

    /**
     * Logs an action with detailed state changes for deep auditing.
     *
     * @param previousState The JSON or string representation of data BEFORE the
     *                      change.
     * @param newState      The state AFTER the change.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(
            AuditAction action,
            User actor,
            String targetType,
            Long targetId,
            String description,
            String previousState,
            String newState) {

        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        AuditLog auditLog = AuditLog.builder()
                .correlationId(correlationId)
                .actorId(actor.getId())
                .actorEmail(actor.getEmail())
                .actorRole(actor.getRole())
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .previousState(previousState)
                .newState(newState)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();

        AuditLog saved = auditLogRepository.save(auditLog);

        log.info("Audit: {} by {} on {}:{} - {}",
                action, actor.getEmail(), targetType, targetId, description);

        return saved;
    }

    /**
     * Logs an action asynchronously.
     * <p>
     * <strong>Performance Optimization:</strong> Use this for non-critical audit
     * events
     * where blocking the user's request thread for an I/O operation is undesirable.
     * Errors in the background thread are caught and logged without affecting the
     * user.
     * </p>
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActionAsync(AuditAction action, User actor, String targetType, Long targetId, String description) {
        try {
            logAction(action, actor, targetType, targetId, description);
        } catch (Exception e) {
            log.error("Failed to log audit entry asynchronously: {}", e.getMessage(), e);
        }
    }

    /**
     * Finds audit logs with filtering and pagination.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByFilters(
            Long actorId,
            AuditAction action,
            String targetType,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        return auditLogRepository.findByFilters(actorId, action, targetType, fromDate, toDate, pageable);
    }

    /**
     * Finds recent audit logs.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findRecent(Pageable pageable) {
        return auditLogRepository.findRecent(pageable);
    }

    /**
     * Finds audit logs for a specific target.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByTarget(String targetType, Long targetId, Pageable pageable) {
        return auditLogRepository.findByTarget(targetType, targetId, pageable);
    }

    /**
     * Finds audit logs by actor.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByActor(Long actorId, Pageable pageable) {
        return auditLogRepository.findByActorId(actorId, pageable);
    }

    // ==================== Helper Methods ====================

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not determine client IP: {}", e.getMessage());
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String userAgent = attrs.getRequest().getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    userAgent = userAgent.substring(0, 500);
                }
                return userAgent;
            }
        } catch (Exception e) {
            log.debug("Could not determine user agent: {}", e.getMessage());
        }
        return null;
    }
}
