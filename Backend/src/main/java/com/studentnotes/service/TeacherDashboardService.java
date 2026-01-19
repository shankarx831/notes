package com.studentnotes.service;

import com.studentnotes.dto.response.TeacherDashboardResponse;
import com.studentnotes.dto.response.NoteResponse;
import com.studentnotes.dto.response.DeletionRequestResponse;
import com.studentnotes.model.DeletionRequest;
import com.studentnotes.model.Note;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.DeletionRequestStatus;
import com.studentnotes.model.enums.NoteStatus;
import com.studentnotes.repository.DeletionRequestRepository;
import com.studentnotes.repository.NoteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for teacher dashboard operations.
 */
@Service
public class TeacherDashboardService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private DeletionRequestRepository deletionRequestRepository;

    @Autowired
    private NoteService noteService;

    @Autowired
    private DeletionRequestService deletionRequestService;

    /**
     * Gets the teacher's dashboard overview.
     */
    @Transactional(readOnly = true)
    public TeacherDashboardResponse getDashboard(User teacher, Pageable pageable) {
        Long teacherId = teacher.getId();

        // Get summary statistics
        TeacherDashboardResponse.NoteSummary summary = getSummary(teacherId);

        // Get notes with pagination
        Page<Note> notesPage = noteRepository.findByUploadedByUserIdOrderByUpdatedAtDesc(teacherId, pageable);
        List<NoteResponse> notes = notesPage.getContent().stream()
                .map(noteService::toListResponse)
                .toList();

        // Group notes by folder
        Map<String, List<NoteResponse>> notesByFolder = notes.stream()
                .collect(Collectors.groupingBy(NoteResponse::getFolderPath));

        // Get pending/recent deletion requests
        List<DeletionRequest> deletionRequests = deletionRequestRepository.findByTeacherId(teacherId);
        List<DeletionRequestResponse> deletionRequestResponses = deletionRequests.stream()
                .map(deletionRequestService::toResponse)
                .toList();

        return TeacherDashboardResponse.builder()
                .summary(summary)
                .notes(notes)
                .notesByFolder(notesByFolder)
                .deletionRequests(deletionRequestResponses)
                .build();
    }

    /**
     * Gets the teacher's notes with pagination.
     */
    @Transactional(readOnly = true)
    public Page<NoteResponse> getNotes(User teacher, NoteStatus status, Pageable pageable) {
        Page<Note> notesPage;

        if (status != null) {
            notesPage = noteRepository.findByUploadedByUserIdAndStatus(teacher.getId(), status, pageable);
        } else {
            notesPage = noteRepository.findByUploadedByUserIdOrderByUpdatedAtDesc(teacher.getId(), pageable);
        }

        return notesPage.map(noteService::toListResponse);
    }

    /**
     * Gets the teacher's deletion requests.
     */
    @Transactional(readOnly = true)
    public Page<DeletionRequestResponse> getDeletionRequests(User teacher, Pageable pageable) {
        Page<DeletionRequest> requestsPage = deletionRequestRepository
                .findByTeacherIdOrderByRequestedAtDesc(teacher.getId(), pageable);

        return requestsPage.map(deletionRequestService::toResponse);
    }

    /**
     * Gets summary statistics for a teacher.
     */
    @Transactional(readOnly = true)
    public TeacherDashboardResponse.NoteSummary getSummary(Long teacherId) {
        long totalNotes = noteRepository.countByUploadedByUserId(teacherId);
        long draftNotes = noteRepository.countByUploadedByUserIdAndStatus(teacherId, NoteStatus.DRAFT);
        long publishedNotes = noteRepository.countByUploadedByUserIdAndStatus(teacherId, NoteStatus.PUBLISHED);
        long deletePendingNotes = noteRepository.countByUploadedByUserIdAndStatus(teacherId, NoteStatus.DELETE_PENDING);
        long deletedNotes = noteRepository.countByUploadedByUserIdAndStatus(teacherId, NoteStatus.DELETED);
        long pendingRequests = deletionRequestRepository.countByStatus(DeletionRequestStatus.PENDING);

        return TeacherDashboardResponse.NoteSummary.builder()
                .totalNotes(totalNotes)
                .draftNotes(draftNotes)
                .publishedNotes(publishedNotes)
                .deletePendingNotes(deletePendingNotes)
                .deletedNotes(deletedNotes)
                .pendingDeletionRequests(pendingRequests)
                .build();
    }
}
