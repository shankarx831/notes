package com.studentnotes.service;

import com.studentnotes.dto.response.AdminOverviewResponse;
import com.studentnotes.model.enums.DeletionRequestStatus;
import com.studentnotes.model.enums.NoteStatus;
import com.studentnotes.model.enums.Role;
import com.studentnotes.model.enums.UserStatus;
import com.studentnotes.repository.DeletionRequestRepository;
import com.studentnotes.repository.NoteRepository;
import com.studentnotes.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdminDashboardService.
 * Tests dashboard aggregation and graceful degradation.
 */
@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeletionRequestRepository deletionRequestRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Nested
    @DisplayName("Get Overview")
    class GetOverviewTests {

        @Test
        @DisplayName("should return complete overview with all metrics")
        void shouldReturnCompleteOverview() {
            // Given
            when(noteRepository.count()).thenReturn(100L);
            when(noteRepository.countByStatus(NoteStatus.PUBLISHED)).thenReturn(80L);
            when(noteRepository.countByStatus(NoteStatus.DRAFT)).thenReturn(15L);
            when(noteRepository.countByStatus(NoteStatus.DELETED)).thenReturn(3L);
            when(noteRepository.countByStatus(NoteStatus.ARCHIVED)).thenReturn(2L);
            when(noteRepository.countByStatus(NoteStatus.DELETE_PENDING)).thenReturn(0L);

            when(userRepository.count()).thenReturn(50L);
            when(userRepository.countByRoleAndStatus(Role.ROLE_TEACHER.getValue(), UserStatus.ACTIVE))
                    .thenReturn(45L);
            when(userRepository.countByRoleAndStatus(Role.ROLE_TEACHER.getValue(), UserStatus.DISABLED))
                    .thenReturn(5L);

            when(deletionRequestRepository.countByStatus(DeletionRequestStatus.PENDING))
                    .thenReturn(2L);

            when(noteRepository.countCreatedSince(any(LocalDateTime.class))).thenReturn(10L, 50L, 100L);
            when(deletionRequestRepository.countCreatedSince(any(LocalDateTime.class))).thenReturn(1L, 5L);

            // When
            AdminOverviewResponse overview = adminDashboardService.getOverview();

            // Then
            assertThat(overview).isNotNull();
            assertThat(overview.getTotalNotes()).isEqualTo(100);
            assertThat(overview.getPublishedNotes()).isEqualTo(80);
            assertThat(overview.getDraftNotes()).isEqualTo(15);
            assertThat(overview.getDeletedNotes()).isEqualTo(3);
            assertThat(overview.getArchivedNotes()).isEqualTo(2);

            assertThat(overview.getTotalUsers()).isEqualTo(50);
            assertThat(overview.getActiveTeachers()).isEqualTo(45);
            assertThat(overview.getDisabledTeachers()).isEqualTo(5);

            assertThat(overview.getPendingDeletionRequests()).isEqualTo(2);

            assertThat(overview.getRecentActivity()).isNotNull();
            assertThat(overview.getHealthStatus()).isNotNull();
            assertThat(overview.getHealthStatus().isDatabaseHealthy()).isTrue();
            assertThat(overview.getComputedAt()).isNotNull();
        }

        @Test
        @DisplayName("should handle note count failure gracefully")
        void shouldHandleNoteCountFailureGracefully() {
            // Given - note counts fail
            when(noteRepository.count()).thenThrow(new RuntimeException("DB connection error"));

            // Other counts succeed
            when(userRepository.count()).thenReturn(50L);
            when(userRepository.countByRoleAndStatus(eq(Role.ROLE_TEACHER.getValue()), any()))
                    .thenReturn(45L);
            when(deletionRequestRepository.countByStatus(DeletionRequestStatus.PENDING))
                    .thenReturn(2L);
            when(noteRepository.countCreatedSince(any(LocalDateTime.class))).thenReturn(0L);
            when(deletionRequestRepository.countCreatedSince(any(LocalDateTime.class))).thenReturn(0L);

            // When
            AdminOverviewResponse overview = adminDashboardService.getOverview();

            // Then - should still return a response with partial data
            assertThat(overview).isNotNull();
            assertThat(overview.getTotalNotes()).isEqualTo(-1); // Error indicator
            assertThat(overview.getTotalUsers()).isEqualTo(50); // This still worked
            assertThat(overview.getPendingDeletionRequests()).isEqualTo(2); // This still worked
        }

        @Test
        @DisplayName("should handle user count failure gracefully")
        void shouldHandleUserCountFailureGracefully() {
            // Given
            when(noteRepository.count()).thenReturn(100L);
            when(noteRepository.countByStatus(any())).thenReturn(25L);

            // User counts fail
            when(userRepository.count()).thenThrow(new RuntimeException("DB error"));

            when(deletionRequestRepository.countByStatus(DeletionRequestStatus.PENDING))
                    .thenReturn(2L);
            when(noteRepository.countCreatedSince(any(LocalDateTime.class))).thenReturn(0L);
            when(deletionRequestRepository.countCreatedSince(any(LocalDateTime.class))).thenReturn(0L);

            // When
            AdminOverviewResponse overview = adminDashboardService.getOverview();

            // Then
            assertThat(overview).isNotNull();
            assertThat(overview.getTotalNotes()).isEqualTo(100);
            assertThat(overview.getTotalUsers()).isEqualTo(-1); // Error indicator
        }

        @Test
        @DisplayName("should handle all failures gracefully")
        void shouldHandleAllFailuresGracefully() {
            // Given - everything fails
            when(noteRepository.count()).thenThrow(new RuntimeException("DB error"));
            when(userRepository.count()).thenThrow(new RuntimeException("DB error"));
            when(deletionRequestRepository.countByStatus(any()))
                    .thenThrow(new RuntimeException("DB error"));

            // When
            AdminOverviewResponse overview = adminDashboardService.getOverview();

            // Then - should still return a response
            assertThat(overview).isNotNull();
            assertThat(overview.getTotalNotes()).isEqualTo(-1);
            assertThat(overview.getTotalUsers()).isEqualTo(-1);
            assertThat(overview.getPendingDeletionRequests()).isEqualTo(-1);
            assertThat(overview.getHealthStatus()).isNotNull();
            assertThat(overview.getHealthStatus().isDatabaseHealthy()).isFalse();
        }

        @Test
        @DisplayName("should include time-bucketed metrics")
        void shouldIncludeTimeBucketedMetrics() {
            // Given
            when(noteRepository.count()).thenReturn(100L);
            when(noteRepository.countByStatus(any())).thenReturn(25L);
            when(userRepository.count()).thenReturn(50L);
            when(userRepository.countByRoleAndStatus(any(), any())).thenReturn(25L);
            when(deletionRequestRepository.countByStatus(any())).thenReturn(2L);

            when(noteRepository.countCreatedSince(any(LocalDateTime.class)))
                    .thenReturn(5L) // last 24h
                    .thenReturn(30L) // last 7d
                    .thenReturn(100L); // last 30d
            when(deletionRequestRepository.countCreatedSince(any(LocalDateTime.class)))
                    .thenReturn(1L) // last 24h
                    .thenReturn(5L); // last 7d

            // When
            AdminOverviewResponse overview = adminDashboardService.getOverview();

            // Then
            assertThat(overview.getRecentActivity()).isNotNull();
            assertThat(overview.getRecentActivity().getNotesUploadedLast24h()).isEqualTo(5);
            assertThat(overview.getRecentActivity().getNotesUploadedLast7d()).isEqualTo(30);
            assertThat(overview.getRecentActivity().getNotesUploadedLast30d()).isEqualTo(100);
            assertThat(overview.getRecentActivity().getDeletionRequestsLast24h()).isEqualTo(1);
            assertThat(overview.getRecentActivity().getDeletionRequestsLast7d()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Get Pending Deletion Request Count")
    class GetPendingCountTests {

        @Test
        @DisplayName("should return correct pending count")
        void shouldReturnCorrectPendingCount() {
            when(deletionRequestRepository.countByStatus(DeletionRequestStatus.PENDING))
                    .thenReturn(5L);

            long count = adminDashboardService.getPendingDeletionRequestCount();

            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("should return -1 on failure")
        void shouldReturnNegativeOneOnFailure() {
            when(deletionRequestRepository.countByStatus(DeletionRequestStatus.PENDING))
                    .thenThrow(new RuntimeException("DB error"));

            long count = adminDashboardService.getPendingDeletionRequestCount();

            assertThat(count).isEqualTo(-1);
        }
    }
}
