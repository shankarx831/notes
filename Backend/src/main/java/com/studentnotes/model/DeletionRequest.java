package com.studentnotes.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class DeletionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Note note;

    @ManyToOne
    private User teacher;

    private String reason;
    
    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, APPROVED, REJECTED

    private LocalDateTime requestedAt;

    public enum Status { PENDING, APPROVED, REJECTED }
}