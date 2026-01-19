package com.studentnotes.dto.response;

import com.studentnotes.model.enums.DeletionRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for deletion request list item.
 * Never exposes raw DB IDs - uses publicId instead.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletionRequestResponse {

    private String publicId;

    // Note information
    private NoteInfoDto note;

    // Requester information
    private UserInfoDto requestedBy;

    private String reason;
    private DeletionRequestStatus status;
    private LocalDateTime requestedAt;

    // Resolution information (if resolved)
    private ResolutionInfoDto resolution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteInfoDto {
        private String publicId;
        private String title;
        private String department;
        private String year;
        private String section;
        private String subject;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto {
        private String publicId;
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolutionInfoDto {
        private UserInfoDto resolvedBy;
        private LocalDateTime resolvedAt;
        private String rejectionReason;
    }
}
