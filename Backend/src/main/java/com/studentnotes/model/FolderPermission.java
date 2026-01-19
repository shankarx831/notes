package com.studentnotes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents folder-level permissions for a user.
 * Permissions are hierarchical - access to a parent grants access to children.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "folder_permissions", uniqueConstraints = @UniqueConstraint(columnNames = { "userId",
        "folderPath" }), indexes = {
                @Index(name = "idx_folder_perm_user", columnList = "userId"),
                @Index(name = "idx_folder_perm_path", columnList = "folderPath")
        })
public class FolderPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user this permission applies to.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * The folder path (e.g., "it/year2/section-a", "cs/year1").
     * Permissions are hierarchical - "it" grants access to all under "it/*".
     */
    @Column(nullable = false, length = 500)
    private String folderPath;

    /**
     * Can the user read notes in this folder?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canRead = true;

    /**
     * Can the user create/upload notes in this folder?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canWrite = false;

    /**
     * Can the user delete notes in this folder (request deletion)?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canDelete = false;

    /**
     * Can the user manage (publish/archive) notes in this folder?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canManage = false;

    /**
     * Who granted this permission.
     */
    @Column(nullable = false)
    private Long grantedByUserId;

    /**
     * When the permission was granted.
     */
    @Column(nullable = false)
    private java.time.LocalDateTime grantedAt;

    /**
     * Optional expiry date for temporary permissions.
     */
    private java.time.LocalDateTime expiresAt;

    /**
     * Whether this permission is currently active.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (grantedAt == null) {
            grantedAt = java.time.LocalDateTime.now();
        }
    }

    /**
     * Checks if this permission covers the given path.
     * A permission on "it" covers "it/year2/section-a/networks".
     */
    public boolean coversPath(String targetPath) {
        if (targetPath == null)
            return false;
        return targetPath.equals(folderPath) || targetPath.startsWith(folderPath + "/");
    }

    /**
     * Checks if this permission is currently valid (active and not expired).
     */
    public boolean isValid() {
        if (!isActive)
            return false;
        if (expiresAt != null && expiresAt.isBefore(java.time.LocalDateTime.now())) {
            return false;
        }
        return true;
    }
}
