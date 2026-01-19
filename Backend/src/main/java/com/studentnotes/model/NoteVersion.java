package com.studentnotes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a specific version of a note.
 * Notes are immutable once versioned - edits create new versions.
 * This ensures full history tracking and rollback capability.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "note_versions", indexes = {
        @Index(name = "idx_note_version_note", columnList = "noteId"),
        @Index(name = "idx_note_version_number", columnList = "noteId, versionNumber")
})
public class NoteVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent note this version belongs to.
     */
    @Column(nullable = false)
    private Long noteId;

    /**
     * Sequential version number (1, 2, 3, ...).
     */
    @Column(nullable = false)
    private Integer versionNumber;

    /**
     * Title at this version.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Content (markdown) at this version.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * File size in bytes (for file uploads).
     */
    private Long fileSizeBytes;

    /**
     * MIME type if file-based.
     */
    @Column(length = 100)
    private String mimeType;

    /**
     * Checksum/hash of content for integrity verification.
     */
    @Column(length = 64)
    private String contentHash;

    /**
     * Who created this version.
     */
    @Column(nullable = false)
    private Long createdByUserId;

    /**
     * Email at time of creation (denormalized).
     */
    @Column(nullable = false)
    private String createdByEmail;

    /**
     * When this version was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Optional: Change summary/commit message.
     */
    @Column(length = 500)
    private String changeSummary;

    /**
     * Whether this is the current active version.
     * Only one version per note should have this true.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isCurrentVersion = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
