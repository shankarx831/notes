package com.studentnotes.model;

import com.studentnotes.model.enums.NoteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a note in the system.
 * Uses explicit state machine via NoteStatus enum.
 * Supports versioning - content changes create new NoteVersion entries.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notes", indexes = {
        @Index(name = "idx_note_status", columnList = "status"),
        @Index(name = "idx_note_uploader", columnList = "uploadedByUserId"),
        @Index(name = "idx_note_department", columnList = "department"),
        @Index(name = "idx_note_folder", columnList = "department, year, section, subject"),
        @Index(name = "idx_note_created", columnList = "createdAt")
})
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public-facing unique identifier.
     * Use this in API responses instead of exposing raw DB IDs.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(nullable = false)
    private String title;

    // Hierarchy Fields (Mirroring folder structure)
    @Column(nullable = false)
    private String department; // e.g., 'it', 'cs'

    @Column(nullable = false)
    private String year; // 'year2'

    private String section; // 'section-a' (optional)

    @Column(nullable = false)
    private String subject; // 'networks'

    @Column(columnDefinition = "TEXT")
    private String content; // Markdown content (current version)

    @Column(length = 10)
    private String type; // "md" for markdown

    /**
     * Current version number (incremented on each edit).
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer currentVersion = 1;

    /**
     * Explicit state machine - replaces boolean 'enabled' flag.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NoteStatus status = NoteStatus.DRAFT;

    // Uploader information (denormalized for performance)
    private Long uploadedByUserId;
    private String uploadedByEmail;
    private String uploadedByName;

    // Vote counts
    private int likes = 0;
    private int dislikes = 0;

    // File metadata (for uploaded files)
    private Long fileSizeBytes;

    @Column(length = 100)
    private String mimeType;

    // Timestamps
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = NoteStatus.DRAFT;
        }
        if (currentVersion == null) {
            currentVersion = 1;
        }
        if (type == null) {
            type = "md";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the folder path for this note.
     * Used for permission checking.
     */
    public String getFolderPath() {
        StringBuilder path = new StringBuilder(department);
        if (year != null) {
            path.append("/").append(year);
        }
        if (section != null) {
            path.append("/").append(section);
        }
        if (subject != null) {
            path.append("/").append(subject);
        }
        return path.toString();
    }

    /**
     * Validates and performs a status transition.
     * 
     * @throws IllegalStateException if transition is not allowed
     */
    public void transitionTo(NoteStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition note from %s to %s", status, newStatus));
        }

        this.status = newStatus;

        // Update relevant timestamps
        switch (newStatus) {
            case PUBLISHED -> this.publishedAt = LocalDateTime.now();
            case DELETED -> this.deletedAt = LocalDateTime.now();
            default -> {
            }
        }
    }

    /**
     * Checks if the note is visible to students.
     */
    public boolean isVisibleToStudents() {
        return status == NoteStatus.PUBLISHED;
    }

    /**
     * Legacy compatibility - maps to status check.
     * 
     * @deprecated Use status field instead
     */
    @Deprecated
    public boolean isEnabled() {
        return status != NoteStatus.DELETED && status != NoteStatus.ARCHIVED;
    }
}