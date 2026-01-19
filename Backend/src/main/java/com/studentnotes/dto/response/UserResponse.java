package com.studentnotes.dto.response;

import com.studentnotes.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for user/teacher details in API responses.
 * Never exposes raw DB IDs or passwords.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String publicId;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;
    private UserStatus status;
    private String statusDescription;

    // Assigned departments
    private List<String> assignedDepartments;

    // Permission summary
    private List<FolderPermissionDto> permissions;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime disabledAt;

    // Creator info (for teachers created by admin)
    private String createdByEmail;

    // Statistics (optional - for dashboard views)
    private UserStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FolderPermissionDto {
        private String folderPath;
        private boolean canRead;
        private boolean canWrite;
        private boolean canDelete;
        private boolean canManage;
        private LocalDateTime grantedAt;
        private LocalDateTime expiresAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatistics {
        private long totalNotesUploaded;
        private long publishedNotes;
        private long draftNotes;
        private long pendingDeletionRequests;
    }
}
