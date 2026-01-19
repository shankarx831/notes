package com.studentnotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for teacher dashboard data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDashboardResponse {

    // Summary statistics
    private NoteSummary summary;

    // Notes grouped by folder
    private Map<String, List<NoteResponse>> notesByFolder;

    // Ungrouped notes list (for flat view)
    private List<NoteResponse> notes;

    // Active deletion requests
    private List<DeletionRequestResponse> deletionRequests;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteSummary {
        private long totalNotes;
        private long draftNotes;
        private long publishedNotes;
        private long deletePendingNotes;
        private long deletedNotes;
        private long pendingDeletionRequests;
    }
}
