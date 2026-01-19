package com.studentnotes.service;

import com.studentnotes.config.CorrelationIdFilter;
import com.studentnotes.dto.request.CreateDeletionRequest;
import com.studentnotes.dto.response.DeletionRequestResponse;
import com.studentnotes.exception.BusinessRuleViolationException;
import com.studentnotes.exception.ResourceNotFoundException;
import com.studentnotes.model.DeletionRequest;
import com.studentnotes.model.Note;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.AuditAction;
import com.studentnotes.model.enums.DeletionRequestStatus;
import com.studentnotes.model.enums.NoteStatus;
import com.studentnotes.repository.DeletionRequestRepository;
import com.studentnotes.repository.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing deletion requests.
 * Handles the lifecycle of deletion requests with proper audit logging.
 */
@Service
public class DeletionRequestService {

    private static final Logger log = LoggerFactory.getLogger(DeletionRequestService.class);

    @Autowired
    private DeletionRequestRepository deletionRequestRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Creates a new deletion request for a note.
     * Enforces: one active request per note, duplicate rejection.
     */
    @Transactional
    public DeletionRequest createRequest(User teacher, String notePublicId, CreateDeletionRequest request) {
        // Find the note
        Note note = noteRepository.findByPublicId(notePublicId)
                .orElseThrow(() -> ResourceNotFoundException.note(notePublicId));

        // Check for existing pending request
        if (deletionRequestRepository.existsPendingRequestForNote(note.getId())) {
            log.warn("Duplicate deletion request attempted by {} for note {}",
                    teacher.getEmail(), notePublicId);
            throw BusinessRuleViolationException.duplicateDeletionRequest(notePublicId);
        }

        // Validate note can be deleted (must be PUBLISHED)
        if (note.getStatus() != NoteStatus.PUBLISHED) {
            throw new BusinessRuleViolationException(
                    "Only published notes can have deletion requests. Current status: " + note.getStatus(),
                    "INVALID_NOTE_STATUS");
        }

        // Create the request
        DeletionRequest deletionRequest = DeletionRequest.builder()
                .note(note)
                .teacher(teacher)
                .reason(request.getReason())
                .status(DeletionRequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        DeletionRequest saved = deletionRequestRepository.save(deletionRequest);

        // Update note status
        note.transitionTo(NoteStatus.DELETE_PENDING);
        noteRepository.save(note);

        // Audit log
        auditService.logAction(
                AuditAction.DELETION_REQUESTED,
                teacher,
                "Note",
                note.getId(),
                String.format("Deletion requested for note '%s': %s", note.getTitle(), request.getReason()));

        log.info("Deletion request {} created by {} for note {}",
                saved.getPublicId(), teacher.getEmail(), notePublicId);

        return saved;
    }

    /**
     * Approves a deletion request.
     * Idempotent: double-clicks are handled safely.
     */
    @Transactional
    public DeletionRequest approveRequest(User admin, String requestPublicId) {
        String idempotencyKey = CorrelationIdFilter.getCurrentCorrelationId();

        DeletionRequest request = deletionRequestRepository.findByPublicId(requestPublicId)
                .orElseThrow(() -> ResourceNotFoundException.deletionRequest(requestPublicId));

        // Idempotency check: if already resolved with same key, return success
        if (request.isResolved()) {
            if (idempotencyKey.equals(request.getResolutionIdempotencyKey())) {
                log.info("Idempotent approval detected for request {}", requestPublicId);
                return request;
            }
            throw BusinessRuleViolationException.alreadyResolved();
        }

        // Approve the request
        request.approve(admin, idempotencyKey);

        // Soft delete the note
        Note note = request.getNote();
        String previousStatus = note.getStatus().name();
        note.transitionTo(NoteStatus.DELETED);
        noteRepository.save(note);

        DeletionRequest saved = deletionRequestRepository.save(request);

        // Audit log
        auditService.logAction(
                AuditAction.DELETION_APPROVED,
                admin,
                "DeletionRequest",
                request.getId(),
                String.format("Approved deletion of note '%s' (requested by %s)",
                        note.getTitle(), request.getTeacher().getEmail()),
                previousStatus,
                NoteStatus.DELETED.name());

        log.info("Deletion request {} approved by {} - note {} soft deleted",
                requestPublicId, admin.getEmail(), note.getPublicId());

        return saved;
    }

    /**
     * Rejects a deletion request.
     * Idempotent: double-clicks are handled safely.
     */
    @Transactional
    public DeletionRequest rejectRequest(User admin, String requestPublicId, String rejectionReason) {
        String idempotencyKey = CorrelationIdFilter.getCurrentCorrelationId();

        DeletionRequest request = deletionRequestRepository.findByPublicId(requestPublicId)
                .orElseThrow(() -> ResourceNotFoundException.deletionRequest(requestPublicId));

        // Idempotency check
        if (request.isResolved()) {
            if (idempotencyKey.equals(request.getResolutionIdempotencyKey())) {
                log.info("Idempotent rejection detected for request {}", requestPublicId);
                return request;
            }
            throw BusinessRuleViolationException.alreadyResolved();
        }

        // Reject the request
        request.reject(admin, rejectionReason, idempotencyKey);

        // Revert note status to PUBLISHED
        Note note = request.getNote();
        String previousStatus = note.getStatus().name();
        note.transitionTo(NoteStatus.PUBLISHED);
        noteRepository.save(note);

        DeletionRequest saved = deletionRequestRepository.save(request);

        // Audit log
        auditService.logAction(
                AuditAction.DELETION_REJECTED,
                admin,
                "DeletionRequest",
                request.getId(),
                String.format("Rejected deletion of note '%s': %s", note.getTitle(), rejectionReason),
                previousStatus,
                NoteStatus.PUBLISHED.name());

        log.info("Deletion request {} rejected by {} - reason: {}",
                requestPublicId, admin.getEmail(), rejectionReason);

        return saved;
    }

    /**
     * Finds all pending deletion requests with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DeletionRequest> findPending(Pageable pageable) {
        return deletionRequestRepository.findByStatus(DeletionRequestStatus.PENDING, pageable);
    }

    /**
     * Finds deletion requests by status with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DeletionRequest> findByStatus(DeletionRequestStatus status, Pageable pageable) {
        return deletionRequestRepository.findByStatus(status, pageable);
    }

    /**
     * Finds deletion requests with filters.
     */
    @Transactional(readOnly = true)
    public Page<DeletionRequest> findByFilters(
            DeletionRequestStatus status,
            Long teacherId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        return deletionRequestRepository.findByFilters(status, teacherId, fromDate, toDate, pageable);
    }

    /**
     * Finds deletion requests by teacher.
     */
    @Transactional(readOnly = true)
    public Page<DeletionRequest> findByTeacher(Long teacherId, Pageable pageable) {
        return deletionRequestRepository.findByTeacherIdOrderByRequestedAtDesc(teacherId, pageable);
    }

    /**
     * Converts a DeletionRequest entity to response DTO.
     */
    public DeletionRequestResponse toResponse(DeletionRequest request) {
        Note note = request.getNote();
        User teacher = request.getTeacher();

        DeletionRequestResponse.ResolutionInfoDto resolution = null;
        if (request.isResolved()) {
            User resolvedBy = request.getResolvedBy();
            resolution = DeletionRequestResponse.ResolutionInfoDto.builder()
                    .resolvedBy(DeletionRequestResponse.UserInfoDto.builder()
                            .publicId(resolvedBy.getPublicId())
                            .name(resolvedBy.getName())
                            .email(resolvedBy.getEmail())
                            .build())
                    .resolvedAt(request.getResolvedAt())
                    .rejectionReason(request.getRejectionReason())
                    .build();
        }

        return DeletionRequestResponse.builder()
                .publicId(request.getPublicId())
                .note(DeletionRequestResponse.NoteInfoDto.builder()
                        .publicId(note.getPublicId())
                        .title(note.getTitle())
                        .department(note.getDepartment())
                        .year(note.getYear())
                        .section(note.getSection())
                        .subject(note.getSubject())
                        .status(note.getStatus().name())
                        .createdAt(note.getCreatedAt())
                        .build())
                .requestedBy(DeletionRequestResponse.UserInfoDto.builder()
                        .publicId(teacher.getPublicId())
                        .name(teacher.getName())
                        .email(teacher.getEmail())
                        .build())
                .reason(request.getReason())
                .status(request.getStatus())
                .requestedAt(request.getRequestedAt())
                .resolution(resolution)
                .build();
    }
}
