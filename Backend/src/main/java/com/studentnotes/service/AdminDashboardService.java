package com.studentnotes.service;

import com.studentnotes.dto.response.AdminOverviewResponse;
import com.studentnotes.model.enums.DeletionRequestStatus;
import com.studentnotes.model.enums.NoteStatus;
import com.studentnotes.model.enums.Role;
import com.studentnotes.model.enums.UserStatus;
import com.studentnotes.repository.DeletionRequestRepository;
import com.studentnotes.repository.NoteRepository;
import com.studentnotes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for admin dashboard operations.
 * Provides aggregated metrics with graceful degradation.
 */
@Service
public class AdminDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeletionRequestRepository deletionRequestRepository;

    /**
     * Gets comprehensive overview metrics for the admin dashboard.
     * Designed for graceful degradation - if a metric fails, others still load.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "adminOverview", key = "'overview'", unless = "#result == null")
    public AdminOverviewResponse getOverview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24h = now.minusHours(24);
        LocalDateTime last7d = now.minusDays(7);
        LocalDateTime last30d = now.minusDays(30);

        AdminOverviewResponse.AdminOverviewResponseBuilder builder = AdminOverviewResponse.builder()
                .computedAt(now);

        // Note counts - with fallback
        try {
            builder.totalNotes(noteRepository.count());
            builder.publishedNotes(noteRepository.countByStatus(NoteStatus.PUBLISHED));
            builder.draftNotes(noteRepository.countByStatus(NoteStatus.DRAFT));
            builder.deletedNotes(noteRepository.countByStatus(NoteStatus.DELETED));
            builder.archivedNotes(noteRepository.countByStatus(NoteStatus.ARCHIVED));
            builder.deletePendingNotes(noteRepository.countByStatus(NoteStatus.DELETE_PENDING));
        } catch (Exception e) {
            log.error("Failed to fetch note counts: {}", e.getMessage());
            // Set defaults for degraded mode
            builder.totalNotes(-1);
        }

        // User counts - with fallback
        try {
            builder.totalUsers(userRepository.count());
            builder.activeTeachers(userRepository.countByRoleAndStatus(
                    Role.ROLE_TEACHER.getValue(), UserStatus.ACTIVE));
            builder.disabledTeachers(userRepository.countByRoleAndStatus(
                    Role.ROLE_TEACHER.getValue(), UserStatus.DISABLED));
        } catch (Exception e) {
            log.error("Failed to fetch user counts: {}", e.getMessage());
            builder.totalUsers(-1);
        }

        // Deletion request counts - with fallback
        try {
            builder.pendingDeletionRequests(deletionRequestRepository.countByStatus(
                    DeletionRequestStatus.PENDING));
        } catch (Exception e) {
            log.error("Failed to fetch deletion request counts: {}", e.getMessage());
            builder.pendingDeletionRequests(-1);
        }

        // Time-bucketed metrics - with fallback
        try {
            AdminOverviewResponse.TimeBucketedMetrics recentActivity = AdminOverviewResponse.TimeBucketedMetrics
                    .builder()
                    .notesUploadedLast24h(noteRepository.countCreatedSince(last24h))
                    .notesUploadedLast7d(noteRepository.countCreatedSince(last7d))
                    .notesUploadedLast30d(noteRepository.countCreatedSince(last30d))
                    .deletionRequestsLast24h(deletionRequestRepository.countCreatedSince(last24h))
                    .deletionRequestsLast7d(deletionRequestRepository.countCreatedSince(last7d))
                    .build();
            builder.recentActivity(recentActivity);
        } catch (Exception e) {
            log.error("Failed to fetch time-bucketed metrics: {}", e.getMessage());
        }

        // Health status
        AdminOverviewResponse.HealthStatus healthStatus = AdminOverviewResponse.HealthStatus.builder()
                .databaseHealthy(isDatabaseHealthy())
                .cacheHealthy(true) // Simplified - in production, check actual cache
                .lastCheckTime(now.toString())
                .readOnlyMode(false) // Could be set by circuit breaker
                .build();
        builder.healthStatus(healthStatus);

        return builder.build();
    }

    /**
     * Gets just the pending counts (fast endpoint for polling).
     */
    @Transactional(readOnly = true)
    public long getPendingDeletionRequestCount() {
        try {
            return deletionRequestRepository.countByStatus(DeletionRequestStatus.PENDING);
        } catch (Exception e) {
            log.error("Failed to fetch pending deletion count: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Checks if the database is healthy.
     */
    private boolean isDatabaseHealthy() {
        try {
            // Simple health check - count query
            noteRepository.count();
            return true;
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
}
