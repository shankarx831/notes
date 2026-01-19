package com.studentnotes.service;

import com.studentnotes.dto.request.CreateTeacherRequest;
import com.studentnotes.dto.request.DisableUserRequest;
import com.studentnotes.dto.request.UpdatePermissionsRequest;
import com.studentnotes.dto.response.UserResponse;
import com.studentnotes.exception.AccessDeniedException;
import com.studentnotes.exception.BusinessRuleViolationException;
import com.studentnotes.exception.ResourceNotFoundException;
import com.studentnotes.model.FolderPermission;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.AuditAction;
import com.studentnotes.model.enums.Role;
import com.studentnotes.model.enums.UserStatus;
import com.studentnotes.repository.NoteRepository;
import com.studentnotes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing users (primarily teachers).
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Creates a new teacher account.
     */
    @Transactional
    public User createTeacher(User admin, CreateTeacherRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessRuleViolationException.emailAlreadyExists(request.getEmail());
        }

        User teacher = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.ROLE_TEACHER.getValue())
                .status(UserStatus.ACTIVE)
                .assignedDepartments(
                        request.getAssignedDepartments() != null ? request.getAssignedDepartments() : List.of())
                .createdByUserId(admin.getId())
                .createdAt(LocalDateTime.now())
                .build();

        User savedTeacher = userRepository.save(teacher);

        // Create default folder permissions based on assigned departments
        if (request.getAssignedDepartments() != null) {
            for (String department : request.getAssignedDepartments()) {
                permissionService.grantPermission(
                        savedTeacher,
                        department, // Department-level permission
                        true, // canRead
                        true, // canWrite
                        false, // canDelete (teachers request deletion)
                        false, // canManage
                        admin,
                        null // No expiry
                );
            }
        }

        // Audit log
        auditService.logAction(
                AuditAction.USER_CREATED,
                admin,
                "User",
                savedTeacher.getId(),
                String.format("Created teacher account for %s (%s)",
                        savedTeacher.getName(), savedTeacher.getEmail()));

        log.info("Teacher {} created by {}", savedTeacher.getEmail(), admin.getEmail());

        return savedTeacher;
    }

    /**
     * Disables a user account (no hard deletes).
     */
    @Transactional
    public User disableUser(User admin, String userPublicId, DisableUserRequest request) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> ResourceNotFoundException.user(userPublicId));

        // Prevent self-disable
        if (user.getId().equals(admin.getId())) {
            throw BusinessRuleViolationException.cannotDeleteSelf();
        }

        // Prevent disabling other admins (unless super-admin logic is implemented)
        if (user.isAdmin()) {
            throw new AccessDeniedException("Cannot disable admin accounts", "CANNOT_DISABLE_ADMIN");
        }

        // Store previous state for audit
        String previousStatus = user.getStatus().name();

        // Disable the user
        user.setStatus(UserStatus.DISABLED);
        user.setDisabledAt(LocalDateTime.now());
        user.setDisabledByUserId(admin.getId());
        user.setDisableReason(request.getReason());

        User savedUser = userRepository.save(user);

        // Audit log
        auditService.logAction(
                AuditAction.USER_DISABLED,
                admin,
                "User",
                savedUser.getId(),
                String.format("Disabled user %s: %s", savedUser.getEmail(), request.getReason()),
                previousStatus,
                UserStatus.DISABLED.name());

        log.info("User {} disabled by {} - reason: {}",
                savedUser.getEmail(), admin.getEmail(), request.getReason());

        return savedUser;
    }

    /**
     * Re-enables a user account.
     */
    @Transactional
    public User enableUser(User admin, String userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> ResourceNotFoundException.user(userPublicId));

        String previousStatus = user.getStatus().name();

        user.setStatus(UserStatus.ACTIVE);
        user.setDisabledAt(null);
        user.setDisabledByUserId(null);
        user.setDisableReason(null);

        User savedUser = userRepository.save(user);

        // Audit log
        auditService.logAction(
                AuditAction.USER_ENABLED,
                admin,
                "User",
                savedUser.getId(),
                String.format("Re-enabled user %s", savedUser.getEmail()),
                previousStatus,
                UserStatus.ACTIVE.name());

        log.info("User {} enabled by {}", savedUser.getEmail(), admin.getEmail());

        return savedUser;
    }

    /**
     * Updates user permissions.
     */
    @Transactional
    public User updatePermissions(User admin, String userPublicId, UpdatePermissionsRequest request) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> ResourceNotFoundException.user(userPublicId));

        // Update assigned departments if provided
        if (request.getAssignedDepartments() != null) {
            user.setAssignedDepartments(request.getAssignedDepartments());
        }

        User savedUser = userRepository.save(user);

        // Update folder permissions
        if (request.getFolderPermissions() != null) {
            for (UpdatePermissionsRequest.FolderPermissionRequest permReq : request.getFolderPermissions()) {
                permissionService.grantPermission(
                        savedUser,
                        permReq.getFolderPath(),
                        permReq.getCanRead(),
                        permReq.getCanWrite(),
                        permReq.getCanDelete(),
                        permReq.getCanManage(),
                        admin,
                        permReq.getExpiresAt());
            }
        }

        // Audit log
        auditService.logAction(
                AuditAction.USER_PERMISSIONS_UPDATED,
                admin,
                "User",
                savedUser.getId(),
                String.format("Updated permissions for user %s", savedUser.getEmail()));

        log.info("Permissions updated for {} by {}", savedUser.getEmail(), admin.getEmail());

        return savedUser;
    }

    /**
     * Finds a user by public ID.
     */
    @Transactional(readOnly = true)
    public User findByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.user(publicId));
    }

    /**
     * Finds a user by email.
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    /**
     * Finds all teachers with pagination.
     */
    @Transactional(readOnly = true)
    public Page<User> findTeachers(Pageable pageable) {
        return userRepository.findByRole(Role.ROLE_TEACHER.getValue(), pageable);
    }

    /**
     * Finds teachers by status.
     */
    @Transactional(readOnly = true)
    public Page<User> findTeachersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByRoleAndStatus(Role.ROLE_TEACHER.getValue(), status, pageable);
    }

    /**
     * Searches teachers by name or email.
     */
    @Transactional(readOnly = true)
    public Page<User> searchTeachers(String query, Pageable pageable) {
        return userRepository.searchByNameOrEmail(Role.ROLE_TEACHER.getValue(), query, pageable);
    }

    /**
     * Converts a User entity to response DTO.
     */
    public UserResponse toResponse(User user) {
        return toResponse(user, false);
    }

    /**
     * Converts a User entity to response DTO with optional statistics.
     */
    public UserResponse toResponse(User user, boolean includeStatistics) {
        List<FolderPermission> permissions = permissionService.getActivePermissions(user);

        UserResponse.UserStatistics stats = null;
        if (includeStatistics) {
            long totalNotes = noteRepository.countByUploadedByUserId(user.getId());
            long publishedNotes = noteRepository.countByUploadedByUserIdAndStatus(
                    user.getId(), com.studentnotes.model.enums.NoteStatus.PUBLISHED);
            long draftNotes = noteRepository.countByUploadedByUserIdAndStatus(
                    user.getId(), com.studentnotes.model.enums.NoteStatus.DRAFT);
            long pendingDeletes = noteRepository.countByUploadedByUserIdAndStatus(
                    user.getId(), com.studentnotes.model.enums.NoteStatus.DELETE_PENDING);

            stats = UserResponse.UserStatistics.builder()
                    .totalNotesUploaded(totalNotes)
                    .publishedNotes(publishedNotes)
                    .draftNotes(draftNotes)
                    .pendingDeletionRequests(pendingDeletes)
                    .build();
        }

        return UserResponse.builder()
                .publicId(user.getPublicId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .statusDescription(user.getStatus().getDescription())
                .assignedDepartments(user.getAssignedDepartments())
                .permissions(permissions.stream()
                        .map(p -> UserResponse.FolderPermissionDto.builder()
                                .folderPath(p.getFolderPath())
                                .canRead(p.getCanRead())
                                .canWrite(p.getCanWrite())
                                .canDelete(p.getCanDelete())
                                .canManage(p.getCanManage())
                                .grantedAt(p.getGrantedAt())
                                .expiresAt(p.getExpiresAt())
                                .build())
                        .toList())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .disabledAt(user.getDisabledAt())
                .statistics(stats)
                .build();
    }
}
