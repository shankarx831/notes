package com.studentnotes.model;

import com.studentnotes.model.enums.Role;
import com.studentnotes.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * User entity with explicit status state machine.
 * No boolean flags - use UserStatus enum instead.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_status", columnList = "status")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public-facing unique identifier.
     * Use this in API responses instead of exposing raw DB IDs.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Will be hashed (BCrypt)

    @Column(nullable = false)
    private String name;

    /**
     * User role - uses Role enum value string for Spring Security compatibility.
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * Explicit status state machine - replaces boolean 'enabled' flag.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    private String phoneNumber;

    /**
     * Departments this user has access to.
     * For more granular control, use FolderPermission entity.
     */
    @ElementCollection
    @CollectionTable(name = "user_departments", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "department")
    private List<String> assignedDepartments;

    // Timestamps
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime disabledAt;

    /**
     * Who created this user (for admin-created teachers).
     */
    private Long createdByUserId;

    /**
     * Who disabled this user (if disabled).
     */
    private Long disabledByUserId;

    /**
     * Reason for disabling (if disabled).
     */
    @Column(length = 500)
    private String disableReason;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if user can log in based on status.
     */
    public boolean canLogin() {
        return status != null && status.canLogin();
    }

    /**
     * Legacy compatibility - maps to status check.
     * 
     * @deprecated Use status field instead
     */
    @Deprecated
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Legacy compatibility setter.
     * 
     * @deprecated Use status field instead
     */
    @Deprecated
    public void setEnabled(boolean enabled) {
        this.status = enabled ? UserStatus.ACTIVE : UserStatus.DISABLED;
    }

    /**
     * Returns the Role enum for this user.
     */
    public Role getRoleEnum() {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Sets role from Role enum.
     */
    public void setRoleEnum(Role role) {
        this.role = role.getValue();
    }

    /**
     * Checks if user is an admin.
     */
    public boolean isAdmin() {
        return Role.ROLE_ADMIN.getValue().equals(role);
    }

    /**
     * Checks if user is a teacher.
     */
    public boolean isTeacher() {
        return Role.ROLE_TEACHER.getValue().equals(role);
    }
}