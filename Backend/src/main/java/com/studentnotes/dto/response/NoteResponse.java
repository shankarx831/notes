package com.studentnotes.dto.response;

import com.studentnotes.model.enums.NoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for note details in API responses.
 * Never exposes raw DB IDs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {

    private String publicId;
    private String title;

    // Folder hierarchy
    private String department;
    private String year;
    private String section;
    private String subject;
    private String folderPath;

    // Content (may be null for list views)
    private String content;
    private String type;

    // Versioning
    private Integer currentVersion;
    private List<VersionInfoDto> versions;

    // Status
    private NoteStatus status;
    private String statusDescription;

    // Uploader info
    private UploaderInfoDto uploadedBy;

    // Metrics
    private int likes;
    private int dislikes;

    // File metadata
    private Long fileSizeBytes;
    private String mimeType;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploaderInfoDto {
        private String publicId;
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionInfoDto {
        private Integer versionNumber;
        private String title;
        private String changeSummary;
        private String createdByEmail;
        private LocalDateTime createdAt;
        private boolean isCurrentVersion;
    }
}
