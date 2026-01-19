package com.studentnotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for updating user permissions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionsRequest {

    /**
     * Departments to assign (replaces existing).
     * Set to null to keep existing departments.
     */
    private List<String> assignedDepartments;

    /**
     * Folder-level permissions to grant/update.
     */
    private List<FolderPermissionRequest> folderPermissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FolderPermissionRequest {

        @NotBlank(message = "Folder path is required")
        private String folderPath;

        @NotNull(message = "canRead is required")
        private Boolean canRead;

        @NotNull(message = "canWrite is required")
        private Boolean canWrite;

        @NotNull(message = "canDelete is required")
        private Boolean canDelete;

        @NotNull(message = "canManage is required")
        private Boolean canManage;

        /**
         * Optional expiry date for temporary permissions.
         */
        private LocalDateTime expiresAt;
    }
}
