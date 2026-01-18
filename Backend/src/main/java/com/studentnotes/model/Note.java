package com.studentnotes.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // Hierarchy Fields (Mirroring folder structure)
    private String department; // e.g., 'it', 'cs'
    private String year; // 'year2'
    private String section; // 'section-a'
    private String subject; // 'networks'

    @Column(columnDefinition = "TEXT")
    private String content; // Markdown content

    private String type; // Always "md" for DB notes

    private Long uploadedByUserId;
    private String uploadedByEmail;
    private String uploadedByName;
    private boolean enabled; // Soft delete flag

    private int likes = 0;
    private int dislikes = 0;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        enabled = true;
        type = "md";
    }
}