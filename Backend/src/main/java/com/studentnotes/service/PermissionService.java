package com.studentnotes.service;

import com.studentnotes.exception.AccessDeniedException;
import com.studentnotes.model.FolderPermission;
import com.studentnotes.model.User;

import com.studentnotes.repository.FolderPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for checking and managing folder-level permissions.
 * Implements hierarchical permission model.
 */
@Service
public class PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private FolderPermissionRepository folderPermissionRepository;

    /**
     * Checks if a user has read access for a specific folder path.
     * <p>
     * <strong>Hierarchical Access Logic:</strong>
     * Access is granted if:
     * <ol>
     * <li>User is an {@code ADMIN}.</li>
     * <li>User belongs to the department mapped to the root of the
     * {@code folderPath}.</li>
     * <li>A specific {@link FolderPermission} entry covers this path or a parent
     * path.</li>
     * </ol>
     * </p>
     *
     * @param user       The user object (must contain loaded department scope).
     * @param folderPath The string path (e.g., "cs/year1/networks").
     * @return true if access is explicitly or implicitly granted.
     */
    @Transactional(readOnly = true)
    public boolean hasReadPermission(User user, String folderPath) {
        // Admins have full access
        if (user.isAdmin()) {
            return true;
        }

        // Check department-level access first (implicit wide-read for dept members)
        if (hasDepartmentAccess(user, folderPath)) {
            return true;
        }

        // Check folder-level permissions (explicit grant)
        return hasPermission(user.getId(), folderPath, PermissionType.READ);
    }

    /**
     * Checks if a user has write permission (upload/edit) for a folder path.
     * <p>
     * <strong>Security Constraint:</strong> Write access is restricted to owners
     * or those with explicit {@code WRITE} permission. Department members
     * do not get implicit write access unless permitted.
     * </p>
     *
     * @param user       The user attempting the write operation.
     * @param folderPath The target folder.
     * @return true if write access is granted.
     */
    @Transactional(readOnly = true)
    public boolean hasWritePermission(User user, String folderPath) {
        if (user.isAdmin()) {
            return true;
        }

        // Teachers usually need to be in the department AND have a WRITE grant
        // Extracting department check to ensure teachers don't write to other
        // departments
        if (!hasDepartmentAccess(user, folderPath)) {
            return false;
        }

        return hasPermission(user.getId(), folderPath, PermissionType.WRITE);
    }

    /**
     * Checks if a user has delete permission for a folder path.
     * <p>
     * Note: Deletions are typically handled via {@code DeletionRequest} workflow,
     * but this check remains for direct operations by authorized roles.
     * </p>
     */
    @Transactional(readOnly = true)
    public boolean hasDeletePermission(User user, String folderPath) {
        if (user.isAdmin()) {
            return true;
        }

        return hasPermission(user.getId(), folderPath, PermissionType.DELETE);
    }

    /**
     * Checks if user has management permissions (publishing drafts or archiving).
     */
    @Transactional(readOnly = true)
    public boolean hasManagePermission(User user, String folderPath) {
        if (user.isAdmin()) {
            return true;
        }

        return hasPermission(user.getId(), folderPath, PermissionType.MANAGE);
    }

    /**
     * Asserts that user has required permission, otherwise throws
     * {@link AccessDeniedException}.
     * 
     * @param user       User to check.
     * @param folderPath Resource path.
     * @param type       Specific permission type required.
     * @throws AccessDeniedException if permission check fails.
     */
    public void assertPermission(User user, String folderPath, PermissionType type) {
        boolean hasPermission = switch (type) {
            case READ -> hasReadPermission(user, folderPath);
            case WRITE -> hasWritePermission(user, folderPath);
            case DELETE -> hasDeletePermission(user, folderPath);
            case MANAGE -> hasManagePermission(user, folderPath);
        };

        if (!hasPermission) {
            log.warn("Permission denied: user {} attempted {} on folder {}",
                    user.getEmail(), type, folderPath);
            throw AccessDeniedException.noFolderPermission(folderPath);
        }
    }

    /**
     * Grants folder permission to a user.
     */
    @Transactional
    public FolderPermission grantPermission(
            User user,
            String folderPath,
            boolean canRead,
            boolean canWrite,
            boolean canDelete,
            boolean canManage,
            User grantedBy,
            LocalDateTime expiresAt) {

        // Check if permission already exists
        FolderPermission existing = folderPermissionRepository
                .findByUserIdAndFolderPath(user.getId(), folderPath)
                .orElse(null);

        if (existing != null) {
            // Update existing
            existing.setCanRead(canRead);
            existing.setCanWrite(canWrite);
            existing.setCanDelete(canDelete);
            existing.setCanManage(canManage);
            existing.setExpiresAt(expiresAt);
            existing.setIsActive(true);
            return folderPermissionRepository.save(existing);
        }

        // Create new permission
        FolderPermission permission = FolderPermission.builder()
                .userId(user.getId())
                .folderPath(folderPath)
                .canRead(canRead)
                .canWrite(canWrite)
                .canDelete(canDelete)
                .canManage(canManage)
                .grantedByUserId(grantedBy.getId())
                .grantedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .isActive(true)
                .build();

        return folderPermissionRepository.save(permission);
    }

    /**
     * Revokes a user's permission for a folder.
     */
    @Transactional
    public void revokePermission(User user, String folderPath) {
        folderPermissionRepository.findByUserIdAndFolderPath(user.getId(), folderPath)
                .ifPresent(permission -> {
                    permission.setIsActive(false);
                    folderPermissionRepository.save(permission);
                });
    }

    /**
     * Gets all active permissions for a user.
     */
    @Transactional(readOnly = true)
    public List<FolderPermission> getActivePermissions(User user) {
        return folderPermissionRepository.findActivePermissions(user.getId(), LocalDateTime.now());
    }

    // ==================== Private Helper Methods ====================

    private boolean hasDepartmentAccess(User user, String folderPath) {
        if (user.getAssignedDepartments() == null || user.getAssignedDepartments().isEmpty()) {
            return false;
        }

        String department = extractDepartment(folderPath);
        return user.getAssignedDepartments().contains(department);
    }

    private String extractDepartment(String folderPath) {
        if (folderPath == null || folderPath.isEmpty()) {
            return "";
        }
        int slashIndex = folderPath.indexOf('/');
        return slashIndex > 0 ? folderPath.substring(0, slashIndex) : folderPath;
    }

    private boolean hasPermission(Long userId, String folderPath, PermissionType type) {
        List<FolderPermission> permissions = folderPermissionRepository
                .findCoveringPermissions(userId, folderPath, LocalDateTime.now());

        if (permissions.isEmpty()) {
            return false;
        }

        // Check if any covering permission grants the required access
        for (FolderPermission permission : permissions) {
            if (permission.isValid()) {
                boolean granted = switch (type) {
                    case READ -> permission.getCanRead();
                    case WRITE -> permission.getCanWrite();
                    case DELETE -> permission.getCanDelete();
                    case MANAGE -> permission.getCanManage();
                };
                if (granted) {
                    return true;
                }
            }
        }

        return false;
    }

    public enum PermissionType {
        READ, WRITE, DELETE, MANAGE
    }
}
