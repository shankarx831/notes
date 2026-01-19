package com.studentnotes.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for admin dashboard overview metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminOverviewResponse {

    // Note counts
    private long totalNotes;
    private long publishedNotes;
    private long draftNotes;
    private long deletedNotes;
    private long archivedNotes;
    private long deletePendingNotes;

    // User counts
    private long totalUsers;
    private long activeTeachers;
    private long disabledTeachers;
    private long pendingDeletionRequests;

    // Time-bucketed metrics
    private TimeBucketedMetrics recentActivity;

    // System health indicators (graceful degradation)
    private HealthStatus healthStatus;

    // When these metrics were computed
    private LocalDateTime computedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeBucketedMetrics {
        private long notesUploadedLast24h;
        private long notesUploadedLast7d;
        private long notesUploadedLast30d;
        private long deletionRequestsLast24h;
        private long deletionRequestsLast7d;
        private long loginsLast24h;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthStatus {
        private boolean databaseHealthy;
        private boolean cacheHealthy;
        private String lastCheckTime;
        private boolean readOnlyMode;
    }
}
