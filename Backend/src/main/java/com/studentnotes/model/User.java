package com.studentnotes.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password; // Will be hashed (BCrypt)

    private String name;

    // Simple Role String: "ROLE_TEACHER", "ROLE_ADMIN"
    private String role;

    private String phoneNumber;

    @ElementCollection
    private java.util.List<String> assignedDepartments;

    private boolean enabled = true;
}