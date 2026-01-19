package com.studentnotes.model;

import com.studentnotes.model.enums.DeletionRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a request from a teacher to delete a note.
 * Requests are immutable after resolution (APPROVED/REJECTED).
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deletion_requests", indexes = {
        @Index(name = "idx_deletion_status", columnList = "status"),
        @Index(name = "idx_deletion_note", columnList = "note_id"),
        @Index(name = "idx_deletion_teacher", columnList = "teacher_id"),
        @Index(name = "idx_deletion_requested_at", columnList = "requestedAt")
})
public class DeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public-facing unique identifier.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    /**
     * Reason for deletion request.
     */
    @Column(nullable = false, length = 1000)
    private String reason;

    /**
     * Current status of the request.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DeletionRequestStatus status = DeletionRequestStatus.PENDING;

    /**
     * When the request was created.
     */
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    // Resolution details (populated when APPROVED or REJECTED)

    /**
     * Who resolved this request (admin).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedBy;

    /**
     * When the request was resolved.
     */
    private LocalDateTime resolvedAt;

    /**
     * Reason for rejection (if REJECTED).
     */
    @Column(length = 1000)
    private String rejectionReason;

    /**
     * Idempotency key to prevent duplicate processing.
     * Set when resolution starts, used for concurrent request handling.
     */
    @Column(length = 36)
    private String resolutionIdempotencyKey;

    @Version
    private Long version; // Optimistic locking for concurrent access

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = DeletionRequestStatus.PENDING;
        }
    }

    /**
     * Checks if this request can still be modified.
     */
    public boolean isResolved() {
        return status != null && status.isResolved();
    }

    /**
     * Checks if this request is pending.
     */
    public boolean isPending() {
        return status == DeletionRequestStatus.PENDING;
    }

    /**
     * Approves this deletion request.
     * 
     * @throws IllegalStateException if already resolved
     */
    public void approve(User admin, String idempotencyKey) {
        if (isResolved()) {
            throw new IllegalStateException("Deletion request has already been resolved");
        }
        this.status = DeletionRequestStatus.APPROVED;
        this.resolvedBy = admin;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionIdempotencyKey = idempotencyKey;
    }

    /**
     * Rejects this deletion request.
     * 
     * @throws IllegalStateException if already resolved
     */
    public void reject(User admin, String rejectionReason, String idempotencyKey) {
        if (isResolved()) {
            throw new IllegalStateException("Deletion request has already been resolved");
        }
        this.status = DeletionRequestStatus.REJECTED;
        this.resolvedBy = admin;
        this.resolvedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
        this.resolutionIdempotencyKey = idempotencyKey;
    }

    // Legacy compatibility

    public enum Status {
        PENDING, APPROVED, REJECTED;

        public DeletionRequestStatus toNew() {
            return DeletionRequestStatus.valueOf(this.name());
        }
    }
}