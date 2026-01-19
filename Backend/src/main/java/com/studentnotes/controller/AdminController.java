package com.studentnotes.controller;

import com.studentnotes.config.RateLimiter;
import com.studentnotes.dto.request.*;
import com.studentnotes.dto.response.*;
import com.studentnotes.exception.RateLimitExceededException;
import com.studentnotes.model.AuditLog;
import com.studentnotes.model.DeletionRequest;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.AuditAction;
import com.studentnotes.model.enums.DeletionRequestStatus;
import com.studentnotes.model.enums.UserStatus;
import com.studentnotes.service.*;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Admin Dashboard API Controller.
 * All endpoints require ADMIN role.
 * 
 * Design principles:
 * - Thin controller, delegates to services
 * - All list endpoints are paginated
 * - Standard API response wrapper
 * - Rate limiting on write endpoints
 * - No business logic in controller
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class AdminController {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private DeletionRequestService deletionRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private RateLimiter rateLimiter;

    // ==================== SYSTEM OVERVIEW ====================

    /**
     * GET /api/admin/overview
     * 
     * Returns aggregated dashboard metrics.
     * Designed for graceful degradation - never fails the UI.
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getOverview() {
        AdminOverviewResponse overview = adminDashboardService.getOverview();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    // ==================== DELETION REQUEST MANAGEMENT ====================

    /**
     * GET /api/admin/deletion-requests
     * 
     * Lists deletion requests with filtering and pagination.
     */
    @GetMapping("/deletion-requests")
    public ResponseEntity<ApiResponse<Page<DeletionRequestResponse>>> getDeletionRequests(
            @RequestParam(required = false) DeletionRequestStatus status,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        Page<DeletionRequest> requestsPage;
        if (status != null || teacherId != null || fromDate != null || toDate != null) {
            requestsPage = deletionRequestService.findByFilters(status, teacherId, fromDate, toDate, pageable);
        } else {
            requestsPage = deletionRequestService.findPending(pageable);
        }

        Page<DeletionRequestResponse> responsePage = requestsPage.map(deletionRequestService::toResponse);

        return ResponseEntity.ok(ApiResponse.success(responsePage, ApiResponse.PageInfo.from(responsePage)));
    }

    /**
     * POST /api/admin/deletion-requests/{publicId}/approve
     * 
     * Approves a deletion request exactly once (idempotent).
     */
    @PostMapping("/deletion-requests/{publicId}/approve")
    public ResponseEntity<ApiResponse<DeletionRequestResponse>> approveDeletionRequest(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = getCurrentUser(userDetails);
        checkRateLimit(admin, true);

        DeletionRequest approved = deletionRequestService.approveRequest(admin, publicId);
        DeletionRequestResponse response = deletionRequestService.toResponse(approved);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/admin/deletion-requests/{publicId}/reject
     * 
     * Rejects a deletion request exactly once (idempotent).
     * Rejection must include a reason.
     */
    @PostMapping("/deletion-requests/{publicId}/reject")
    public ResponseEntity<ApiResponse<DeletionRequestResponse>> rejectDeletionRequest(
            @PathVariable String publicId,
            @Valid @RequestBody RejectDeletionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = getCurrentUser(userDetails);
        checkRateLimit(admin, true);

        DeletionRequest rejected = deletionRequestService.rejectRequest(admin, publicId, request.getReason());
        DeletionRequestResponse response = deletionRequestService.toResponse(rejected);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== USER & PERMISSION MANAGEMENT ====================

    /**
     * GET /api/admin/teachers
     * 
     * Lists all teacher accounts with pagination.
     */
    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getTeachers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        Page<User> usersPage;
        if (search != null && !search.isBlank()) {
            usersPage = userService.searchTeachers(search, pageable);
        } else if (status != null) {
            usersPage = userService.findTeachersByStatus(status, pageable);
        } else {
            usersPage = userService.findTeachers(pageable);
        }

        Page<UserResponse> responsePage = usersPage.map(u -> userService.toResponse(u, true));

        return ResponseEntity.ok(ApiResponse.success(responsePage, ApiResponse.PageInfo.from(responsePage)));
    }

    /**
     * GET /api/admin/teachers/{publicId}
     * 
     * Gets a single teacher's details.
     */
    @GetMapping("/teachers/{publicId}")
    public ResponseEntity<ApiResponse<UserResponse>> getTeacher(@PathVariable String publicId) {
        User teacher = userService.findByPublicId(publicId);
        UserResponse response = userService.toResponse(teacher, true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/admin/teachers
     * 
     * Creates a new teacher account.
     */
    @PostMapping("/teachers")
    public ResponseEntity<ApiResponse<UserResponse>> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = getCurrentUser(userDetails);
        checkRateLimit(admin, true);

        User teacher = userService.createTeacher(admin, request);
        UserResponse response = userService.toResponse(teacher, false);

        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    /**
     * PATCH /api/admin/teachers/{publicId}/permissions
     * 
     * Updates a teacher's permissions.
     */
    @PatchMapping("/teachers/{publicId}/permissions")
    public ResponseEntity<ApiResponse<UserResponse>> updateTeacherPermissions(
            @PathVariable String publicId,
            @Valid @RequestBody UpdatePermissionsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = getCurrentUser(userDetails);
        checkRateLimit(admin, true);

        User teacher = userService.updatePermissions(admin, publicId, request);
        UserResponse response = userService.toResponse(teacher, false);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PATCH /api/admin/users/{publicId}/disable
     * 
     * Disables a user account (no hard deletes).
     */
    @PatchMapping("/users/{publicId}/disable")
    public ResponseEntity<ApiResponse<UserResponse>> disableUser(
            @PathVariable String publicId,
            @Valid @RequestBody DisableUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = getCurrentUser(userDetails);
        checkRateLimit(admin, true);

        User user = userService.disableUser(admin, publicId, request);
        UserResponse response = userService.toResponse(user, false);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PATCH /api/admin/users/{publicId}/enable
     * 
     * Re-enables a disabled user account.
     */
    @PatchMapping("/users/{publicId}/enable")
    public ResponseEntity<ApiResponse<UserResponse>> enableUser(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = getCurrentUser(userDetails);
        checkRateLimit(admin, true);

        User user = userService.enableUser(admin, publicId);
        UserResponse response = userService.toResponse(user, false);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== AUDIT LOGS ====================

    /**
     * GET /api/admin/audit-logs
     * 
     * Lists audit logs with filtering and pagination.
     * Read-only endpoint.
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = createPageable(page, Math.min(size, MAX_PAGE_SIZE), "timestamp", "desc");

        Page<AuditLog> logsPage = auditService.findByFilters(actorId, action, targetType, fromDate, toDate, pageable);
        Page<AuditLogResponse> responsePage = logsPage.map(this::toAuditLogResponse);

        return ResponseEntity.ok(ApiResponse.success(responsePage, ApiResponse.PageInfo.from(responsePage)));
    }

    // ==================== HELPER METHODS ====================

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    private void checkRateLimit(User user, boolean isWriteOperation) {
        if (!rateLimiter.isAllowed(user.getPublicId(), isWriteOperation)) {
            int retryAfter = rateLimiter.getRetryAfterSeconds(user.getPublicId(), isWriteOperation);
            throw new RateLimitExceededException(retryAfter);
        }
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        int clampedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), clampedSize, Sort.by(direction, sortBy));
    }

    private AuditLogResponse toAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .correlationId(log.getCorrelationId())
                .actor(AuditLogResponse.ActorInfoDto.builder()
                        .email(log.getActorEmail())
                        .role(log.getActorRole())
                        .build())
                .action(log.getAction())
                .actionDescription(log.getAction().getDescription())
                .targetType(log.getTargetType())
                .targetDescription(log.getDescription())
                .description(log.getDescription())
                .previousState(log.getPreviousState())
                .newState(log.getNewState())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .build();
    }
}