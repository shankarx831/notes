package com.studentnotes.model;

import com.studentnotes.model.enums.AuditAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit log entry.
 * Every significant action in the system creates an entry here.
 * Append-only - entries are never modified or deleted.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_actor", columnList = "actorId"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_target", columnList = "targetType, targetId")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier for the request that triggered this action.
     * Used for correlation across distributed systems and debugging.
     */
    @Column(nullable = false, length = 36)
    private String correlationId;

    /**
     * The user who performed the action.
     * Stored as ID, not as a FK to preserve history even if user is deleted.
     */
    @Column(nullable = false)
    private Long actorId;

    /**
     * Email of the actor at the time of action.
     * Denormalized for queryability without joins.
     */
    @Column(nullable = false)
    private String actorEmail;

    /**
     * Role of the actor at the time of action.
     */
    @Column(nullable = false)
    private String actorRole;

    /**
     * The action that was performed.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    /**
     * Type of the target entity (e.g., "Note", "User", "DeletionRequest")
     */
    @Column(nullable = false, length = 50)
    private String targetType;

    /**
     * ID of the target entity.
     */
    @Column(nullable = false)
    private Long targetId;

    /**
     * Human-readable description of what changed.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * JSON representation of the state before the action (for updates).
     */
    @Column(columnDefinition = "TEXT")
    private String previousState;

    /**
     * JSON representation of the state after the action.
     */
    @Column(columnDefinition = "TEXT")
    private String newState;

    /**
     * IP address of the client.
     */
    @Column(length = 45) // IPv6 max length
    private String ipAddress;

    /**
     * User agent string.
     */
    @Column(length = 500)
    private String userAgent;

    /**
     * When the action occurred.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Optional: Additional metadata as JSON
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
    }

    /**
     * Builder helper for common audit log creation
     */
    public static AuditLogBuilder forAction(AuditAction action) {
        return AuditLog.builder()
                .action(action)
                .timestamp(LocalDateTime.now());
    }
}
