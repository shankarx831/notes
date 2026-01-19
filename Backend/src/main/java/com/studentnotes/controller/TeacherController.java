package com.studentnotes.controller;

import com.studentnotes.config.RateLimiter;
import com.studentnotes.dto.request.CreateDeletionRequest;
import com.studentnotes.dto.request.CreateNoteRequest;
import com.studentnotes.dto.request.UpdateNoteRequest;
import com.studentnotes.dto.response.*;
import com.studentnotes.exception.AccessDeniedException;
import com.studentnotes.exception.RateLimitExceededException;
import com.studentnotes.model.DeletionRequest;
import com.studentnotes.model.Note;
import com.studentnotes.model.NoteVersion;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.NoteStatus;
import com.studentnotes.service.*;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Teacher Dashboard API Controller.
 * All endpoints require TEACHER role.
 * 
 * Design principles:
 * - Teachers see ONLY their notes
 * - No direct delete - must request deletion
 * - All list endpoints are paginated
 * - Sorted by last updated
 */
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class TeacherController {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private TeacherDashboardService teacherDashboardService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private DeletionRequestService deletionRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private RateLimiter rateLimiter;

    // ==================== DASHBOARD ====================

    /**
     * GET /api/teacher/dashboard
     * 
     * Returns teacher's dashboard overview.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<TeacherDashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User teacher = getCurrentUser(userDetails);
        Pageable pageable = createPageable(page, size, "updatedAt", "desc");

        TeacherDashboardResponse dashboard = teacherDashboardService.getDashboard(teacher, pageable);

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    // ==================== NOTES ====================

    /**
     * GET /api/teacher/notes
     * 
     * Lists teacher's notes with filtering and pagination.
     * Teachers see ONLY their own notes.
     */
    @GetMapping("/notes")
    public ResponseEntity<ApiResponse<Page<NoteResponse>>> getNotes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) NoteStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        User teacher = getCurrentUser(userDetails);
        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        Page<NoteResponse> notesPage = teacherDashboardService.getNotes(teacher, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(notesPage, ApiResponse.PageInfo.from(notesPage)));
    }

    /**
     * GET /api/teacher/notes/{publicId}
     * 
     * Gets a single note's details.
     * Only owner can access.
     */
    @GetMapping("/notes/{publicId}")
    public ResponseEntity<ApiResponse<NoteResponse>> getNote(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User teacher = getCurrentUser(userDetails);
        Note note = noteService.findByPublicId(publicId);

        // Ownership check
        if (!isOwner(teacher, note)) {
            throw AccessDeniedException.notResourceOwner();
        }

        NoteResponse response = noteService.toResponse(note);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/teacher/notes
     * 
     * Creates a new note (upload).
     * New upload creates version 1.
     */
    @PostMapping("/notes")
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User teacher = getCurrentUser(userDetails);
        checkRateLimit(teacher, true);

        Note note = noteService.createNote(teacher, request);
        NoteResponse response = noteService.toResponse(note);

        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    /**
     * PUT /api/teacher/notes/{publicId}
     * 
     * Updates a note, creating a new version.
     * Old versions remain immutable.
     */
    @PutMapping("/notes/{publicId}")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User teacher = getCurrentUser(userDetails);
        checkRateLimit(teacher, true);

        // Ownership check
        Note existingNote = noteService.findByPublicId(publicId);
        if (!isOwner(teacher, existingNote)) {
            throw AccessDeniedException.notResourceOwner();
        }

        Note updatedNote = noteService.updateNote(teacher, publicId, request);
        NoteResponse response = noteService.toResponse(updatedNote);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/teacher/notes/{publicId}/publish
     * 
     * Publishes a draft note.
     */
    @PostMapping("/notes/{publicId}/publish")
    public ResponseEntity<ApiResponse<NoteResponse>> publishNote(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User teacher = getCurrentUser(userDetails);
        checkRateLimit(teacher, true);

        // Ownership check
        Note existingNote = noteService.findByPublicId(publicId);
        if (!isOwner(teacher, existingNote)) {
            throw AccessDeniedException.notResourceOwner();
        }

        Note publishedNote = noteService.publishNote(teacher, publicId);
        NoteResponse response = noteService.toResponse(publishedNote);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/teacher/notes/{publicId}/versions
     * 
     * Gets version history for a note.
     */
    @GetMapping("/notes/{publicId}/versions")
    public ResponseEntity<ApiResponse<List<NoteResponse.VersionInfoDto>>> getNoteVersions(
            @PathVariable String publicId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User teacher = getCurrentUser(userDetails);
        Note note = noteService.findByPublicId(publicId);

        // Ownership check
        if (!isOwner(teacher, note)) {
            throw AccessDeniedException.notResourceOwner();
        }

        List<NoteVersion> versions = noteService.getVersionHistory(publicId);
        List<NoteResponse.VersionInfoDto> response = versions.stream()
                .map(v -> NoteResponse.VersionInfoDto.builder()
                        .versionNumber(v.getVersionNumber())
                        .title(v.getTitle())
                        .changeSummary(v.getChangeSummary())
                        .createdByEmail(v.getCreatedByEmail())
                        .createdAt(v.getCreatedAt())
                        .isCurrentVersion(v.getIsCurrentVersion())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== DELETION REQUESTS ====================

    /**
     * POST /api/teacher/notes/{publicId}/request-delete
     * 
     * Requests deletion of a note (no direct delete allowed).
     * Only one active request per note.
     * Duplicate requests are rejected.
     */
    @PostMapping("/notes/{publicId}/request-delete")
    public ResponseEntity<ApiResponse<DeletionRequestResponse>> requestDelete(
            @PathVariable String publicId,
            @Valid @RequestBody CreateDeletionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User teacher = getCurrentUser(userDetails);
        checkRateLimit(teacher, true);

        // Ownership check
        Note note = noteService.findByPublicId(publicId);
        if (!isOwner(teacher, note)) {
            throw AccessDeniedException.notResourceOwner();
        }

        DeletionRequest deletionRequest = deletionRequestService.createRequest(teacher, publicId, request);
        DeletionRequestResponse response = deletionRequestService.toResponse(deletionRequest);

        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    /**
     * GET /api/teacher/deletion-requests
     * 
     * Lists teacher's deletion requests with pagination.
     */
    @GetMapping("/deletion-requests")
    public ResponseEntity<ApiResponse<Page<DeletionRequestResponse>>> getDeletionRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User teacher = getCurrentUser(userDetails);
        Pageable pageable = createPageable(page, size, "requestedAt", "desc");

        Page<DeletionRequestResponse> requestsPage = teacherDashboardService.getDeletionRequests(teacher, pageable);

        return ResponseEntity.ok(ApiResponse.success(requestsPage, ApiResponse.PageInfo.from(requestsPage)));
    }

    // ==================== HELPER METHODS ====================

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    private boolean isOwner(User teacher, Note note) {
        return note.getUploadedByUserId() != null &&
                note.getUploadedByUserId().equals(teacher.getId());
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
}
