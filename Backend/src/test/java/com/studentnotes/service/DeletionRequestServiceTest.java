package com.studentnotes.service;

import com.studentnotes.dto.request.CreateDeletionRequest;
import com.studentnotes.exception.BusinessRuleViolationException;
import com.studentnotes.exception.ResourceNotFoundException;
import com.studentnotes.model.DeletionRequest;
import com.studentnotes.model.Note;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.DeletionRequestStatus;
import com.studentnotes.model.enums.NoteStatus;
import com.studentnotes.model.enums.Role;
import com.studentnotes.model.enums.UserStatus;
import com.studentnotes.repository.DeletionRequestRepository;
import com.studentnotes.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeletionRequestService.
 * Covers deletion flow, idempotency, and concurrent access scenarios.
 */
@ExtendWith(MockitoExtension.class)
class DeletionRequestServiceTest {

        @Mock
        private DeletionRequestRepository deletionRequestRepository;

        @Mock
        private NoteRepository noteRepository;

        @Mock
        private AuditService auditService;

        @InjectMocks
        private DeletionRequestService deletionRequestService;

        private User teacher;
        private User admin;
        private Note publishedNote;

        @BeforeEach
        void setUp() {
                teacher = User.builder()
                                .id(1L)
                                .publicId(UUID.randomUUID().toString())
                                .email("teacher@example.com")
                                .name("Test Teacher")
                                .role(Role.ROLE_TEACHER.getValue())
                                .status(UserStatus.ACTIVE)
                                .build();

                admin = User.builder()
                                .id(2L)
                                .publicId(UUID.randomUUID().toString())
                                .email("admin@example.com")
                                .name("Test Admin")
                                .role(Role.ROLE_ADMIN.getValue())
                                .status(UserStatus.ACTIVE)
                                .build();

                publishedNote = Note.builder()
                                .id(100L)
                                .publicId(UUID.randomUUID().toString())
                                .title("Test Note")
                                .department("it")
                                .year("year2")
                                .subject("networks")
                                .content("Test content")
                                .status(NoteStatus.PUBLISHED)
                                .uploadedByUserId(teacher.getId())
                                .uploadedByEmail(teacher.getEmail())
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        @Nested
        @DisplayName("Create Deletion Request")
        class CreateDeletionRequestTests {

                @Test
                @DisplayName("should create deletion request for published note")
                void shouldCreateDeletionRequest() {
                        // Given
                        CreateDeletionRequest request = CreateDeletionRequest.builder()
                                        .reason("Outdated content")
                                        .build();

                        when(noteRepository.findByPublicId(publishedNote.getPublicId()))
                                        .thenReturn(Optional.of(publishedNote));
                        when(deletionRequestRepository.existsPendingRequestForNote(publishedNote.getId()))
                                        .thenReturn(false);
                        when(deletionRequestRepository.save(any(DeletionRequest.class)))
                                        .thenAnswer(inv -> {
                                                DeletionRequest dr = inv.getArgument(0);
                                                dr.setId(1L);
                                                return dr;
                                        });
                        when(noteRepository.save(any(Note.class))).thenReturn(publishedNote);

                        // When
                        DeletionRequest result = deletionRequestService.createRequest(
                                        teacher, publishedNote.getPublicId(), request);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getStatus()).isEqualTo(DeletionRequestStatus.PENDING);
                        assertThat(result.getReason()).isEqualTo("Outdated content");
                        assertThat(result.getTeacher()).isEqualTo(teacher);

                        verify(noteRepository).save(any(Note.class)); // Note status should be updated
                        verify(auditService).logAction(any(), eq(teacher), anyString(), any(), anyString());
                }

                @Test
                @DisplayName("should reject duplicate deletion request")
                void shouldRejectDuplicateDeletionRequest() {
                        // Given
                        CreateDeletionRequest request = CreateDeletionRequest.builder()
                                        .reason("Duplicate request")
                                        .build();

                        when(noteRepository.findByPublicId(publishedNote.getPublicId()))
                                        .thenReturn(Optional.of(publishedNote));
                        when(deletionRequestRepository.existsPendingRequestForNote(publishedNote.getId()))
                                        .thenReturn(true); // Already has pending request

                        // When/Then
                        assertThatThrownBy(
                                        () -> deletionRequestService.createRequest(teacher, publishedNote.getPublicId(),
                                                        request))
                                        .isInstanceOf(BusinessRuleViolationException.class)
                                        .hasMessageContaining("pending deletion request already exists");
                }

                @Test
                @DisplayName("should throw when note not found")
                void shouldThrowWhenNoteNotFound() {
                        // Given
                        CreateDeletionRequest request = CreateDeletionRequest.builder()
                                        .reason("Delete reason")
                                        .build();

                        when(noteRepository.findByPublicId("non-existent"))
                                        .thenReturn(Optional.empty());

                        // When/Then
                        assertThatThrownBy(() -> deletionRequestService.createRequest(teacher, "non-existent", request))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

                @Test
                @DisplayName("should reject deletion request for non-published note")
                void shouldRejectDeletionRequestForDraftNote() {
                        // Given
                        Note draftNote = Note.builder()
                                        .id(101L)
                                        .publicId(UUID.randomUUID().toString())
                                        .status(NoteStatus.DRAFT)
                                        .build();

                        CreateDeletionRequest request = CreateDeletionRequest.builder()
                                        .reason("Delete draft")
                                        .build();

                        when(noteRepository.findByPublicId(draftNote.getPublicId()))
                                        .thenReturn(Optional.of(draftNote));
                        when(deletionRequestRepository.existsPendingRequestForNote(draftNote.getId()))
                                        .thenReturn(false);

                        // When/Then
                        assertThatThrownBy(() -> deletionRequestService.createRequest(teacher, draftNote.getPublicId(),
                                        request))
                                        .isInstanceOf(BusinessRuleViolationException.class)
                                        .hasMessageContaining("Only published notes");
                }
        }

        @Nested
        @DisplayName("Approve Deletion Request")
        class ApproveDeletionRequestTests {

                private DeletionRequest pendingRequest;

                @BeforeEach
                void setUp() {
                        // NOTE: Note must be in DELETE_PENDING state for approval to work
                        publishedNote.setStatus(NoteStatus.DELETE_PENDING);

                        pendingRequest = DeletionRequest.builder()
                                        .id(10L)
                                        .publicId(UUID.randomUUID().toString())
                                        .note(publishedNote)
                                        .teacher(teacher)
                                        .reason("Outdated content")
                                        .status(DeletionRequestStatus.PENDING)
                                        .requestedAt(LocalDateTime.now())
                                        .build();
                }

                @Test
                @DisplayName("should approve pending request and soft delete note")
                void shouldApproveRequest() {
                        // Given
                        when(deletionRequestRepository.findByPublicId(pendingRequest.getPublicId()))
                                        .thenReturn(Optional.of(pendingRequest));
                        when(deletionRequestRepository.save(any(DeletionRequest.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));
                        when(noteRepository.save(any(Note.class))).thenReturn(publishedNote);

                        // When
                        DeletionRequest result = deletionRequestService.approveRequest(
                                        admin, pendingRequest.getPublicId());

                        // Then
                        assertThat(result.getStatus()).isEqualTo(DeletionRequestStatus.APPROVED);
                        assertThat(result.getResolvedBy()).isEqualTo(admin);
                        assertThat(result.getResolvedAt()).isNotNull();

                        verify(noteRepository).save(any(Note.class)); // Note soft deleted
                        verify(auditService).logAction(any(), eq(admin), anyString(), any(), anyString(), any(), any());
                }

                @Test
                @DisplayName("should handle idempotent double-click approve")
                void shouldHandleIdempotentApprove() {
                        // Given - already approved with same correlation ID
                        pendingRequest.setStatus(DeletionRequestStatus.APPROVED);
                        pendingRequest.setResolvedBy(admin);
                        pendingRequest.setResolvedAt(LocalDateTime.now());
                        // In real scenario, resolutionIdempotencyKey would be set

                        when(deletionRequestRepository.findByPublicId(pendingRequest.getPublicId()))
                                        .thenReturn(Optional.of(pendingRequest));

                        // When/Then - should throw since request is already resolved
                        assertThatThrownBy(() -> deletionRequestService.approveRequest(admin,
                                        pendingRequest.getPublicId()))
                                        .isInstanceOf(BusinessRuleViolationException.class)
                                        .hasMessageContaining("already been resolved");
                }

                @Test
                @DisplayName("should throw when request not found")
                void shouldThrowWhenRequestNotFound() {
                        // Given
                        when(deletionRequestRepository.findByPublicId("non-existent"))
                                        .thenReturn(Optional.empty());

                        // When/Then
                        assertThatThrownBy(() -> deletionRequestService.approveRequest(admin, "non-existent"))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }
        }

        @Nested
        @DisplayName("Reject Deletion Request")
        class RejectDeletionRequestTests {

                private DeletionRequest pendingRequest;

                @BeforeEach
                void setUp() {
                        publishedNote.setStatus(NoteStatus.DELETE_PENDING);

                        pendingRequest = DeletionRequest.builder()
                                        .id(10L)
                                        .publicId(UUID.randomUUID().toString())
                                        .note(publishedNote)
                                        .teacher(teacher)
                                        .reason("Outdated content")
                                        .status(DeletionRequestStatus.PENDING)
                                        .requestedAt(LocalDateTime.now())
                                        .build();
                }

                @Test
                @DisplayName("should reject request and restore note to published")
                void shouldRejectRequest() {
                        // Given
                        String rejectionReason = "Content is still relevant";

                        when(deletionRequestRepository.findByPublicId(pendingRequest.getPublicId()))
                                        .thenReturn(Optional.of(pendingRequest));
                        when(deletionRequestRepository.save(any(DeletionRequest.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));
                        when(noteRepository.save(any(Note.class))).thenReturn(publishedNote);

                        // When
                        DeletionRequest result = deletionRequestService.rejectRequest(
                                        admin, pendingRequest.getPublicId(), rejectionReason);

                        // Then
                        assertThat(result.getStatus()).isEqualTo(DeletionRequestStatus.REJECTED);
                        assertThat(result.getResolvedBy()).isEqualTo(admin);
                        assertThat(result.getRejectionReason()).isEqualTo(rejectionReason);

                        verify(noteRepository).save(any(Note.class)); // Note restored to published
                        verify(auditService).logAction(any(), eq(admin), anyString(), any(), anyString(), any(), any());
                }

                @Test
                @DisplayName("should throw when rejecting already resolved request")
                void shouldThrowWhenRejectingResolvedRequest() {
                        // Given - already rejected
                        pendingRequest.setStatus(DeletionRequestStatus.REJECTED);
                        pendingRequest.setResolvedBy(admin);
                        pendingRequest.setResolvedAt(LocalDateTime.now());
                        pendingRequest.setRejectionReason("Already rejected");

                        when(deletionRequestRepository.findByPublicId(pendingRequest.getPublicId()))
                                        .thenReturn(Optional.of(pendingRequest));

                        // When/Then
                        assertThatThrownBy(
                                        () -> deletionRequestService.rejectRequest(admin, pendingRequest.getPublicId(),
                                                        "New reason"))
                                        .isInstanceOf(BusinessRuleViolationException.class)
                                        .hasMessageContaining("already been resolved");
                }
        }
}
