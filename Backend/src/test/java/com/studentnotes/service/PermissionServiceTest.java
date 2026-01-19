package com.studentnotes.service;

import com.studentnotes.exception.AccessDeniedException;
import com.studentnotes.model.FolderPermission;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.Role;
import com.studentnotes.model.enums.UserStatus;
import com.studentnotes.repository.FolderPermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PermissionService.
 * Tests hierarchical permission model and folder-level access control.
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private FolderPermissionRepository folderPermissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private User admin;
    private User teacher;
    private User teacherWithDepartment;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .publicId(UUID.randomUUID().toString())
                .email("admin@example.com")
                .role(Role.ROLE_ADMIN.getValue())
                .status(UserStatus.ACTIVE)
                .assignedDepartments(List.of())
                .build();

        teacher = User.builder()
                .id(2L)
                .publicId(UUID.randomUUID().toString())
                .email("teacher@example.com")
                .role(Role.ROLE_TEACHER.getValue())
                .status(UserStatus.ACTIVE)
                .assignedDepartments(List.of())
                .build();

        teacherWithDepartment = User.builder()
                .id(3L)
                .publicId(UUID.randomUUID().toString())
                .email("teacher.it@example.com")
                .role(Role.ROLE_TEACHER.getValue())
                .status(UserStatus.ACTIVE)
                .assignedDepartments(List.of("it"))
                .build();
    }

    @Nested
    @DisplayName("Admin Permissions")
    class AdminPermissionTests {

        @Test
        @DisplayName("admin should have read access to any folder")
        void adminShouldHaveReadAccess() {
            assertThat(permissionService.hasReadPermission(admin, "it/year2/section-a/networks")).isTrue();
            assertThat(permissionService.hasReadPermission(admin, "cs/year1")).isTrue();
            assertThat(permissionService.hasReadPermission(admin, "any/folder/path")).isTrue();
        }

        @Test
        @DisplayName("admin should have write access to any folder")
        void adminShouldHaveWriteAccess() {
            assertThat(permissionService.hasWritePermission(admin, "it/year2/section-a/networks")).isTrue();
            assertThat(permissionService.hasWritePermission(admin, "cs/year1")).isTrue();
        }

        @Test
        @DisplayName("admin should have delete access to any folder")
        void adminShouldHaveDeleteAccess() {
            assertThat(permissionService.hasDeletePermission(admin, "it/year2")).isTrue();
        }

        @Test
        @DisplayName("admin should have manage access to any folder")
        void adminShouldHaveManageAccess() {
            assertThat(permissionService.hasManagePermission(admin, "it/year2")).isTrue();
        }
    }

    @Nested
    @DisplayName("Teacher Department Access")
    class TeacherDepartmentAccessTests {

        @Test
        @DisplayName("teacher with assigned department should have read access")
        void teacherWithDepartmentShouldHaveReadAccess() {
            assertThat(permissionService.hasReadPermission(teacherWithDepartment, "it")).isTrue();
            assertThat(permissionService.hasReadPermission(teacherWithDepartment, "it/year2")).isTrue();
            assertThat(permissionService.hasReadPermission(teacherWithDepartment, "it/year2/section-a")).isTrue();
        }

        @Test
        @DisplayName("teacher without assigned department should not have access")
        void teacherWithoutDepartmentShouldNotHaveReadAccess() {
            when(folderPermissionRepository.findCoveringPermissions(eq(teacher.getId()), any(), any()))
                    .thenReturn(Collections.emptyList());

            assertThat(permissionService.hasReadPermission(teacher, "it/year2")).isFalse();
        }

        @Test
        @DisplayName("teacher should not have access to other departments")
        void teacherShouldNotHaveAccessToOtherDepartments() {
            when(folderPermissionRepository.findCoveringPermissions(eq(teacherWithDepartment.getId()), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Teacher assigned to "it" should not have access to "cs"
            assertThat(permissionService.hasReadPermission(teacherWithDepartment, "cs/year1")).isFalse();
        }
    }

    @Nested
    @DisplayName("Folder-Level Permissions")
    class FolderLevelPermissionTests {

        @Test
        @DisplayName("should grant access when folder permission exists")
        void shouldGrantAccessWithFolderPermission() {
            // Create fresh user with NO department access to ensure we test explicit
            // permissions
            User cleanTeacher = User.builder()
                    .id(99L)
                    .publicId("clean-teacher")
                    .email("clean@test.com")
                    .role(Role.ROLE_TEACHER.getValue())
                    .status(UserStatus.ACTIVE)
                    .assignedDepartments(Collections.emptyList())
                    .build();

            FolderPermission permission = FolderPermission.builder()
                    .userId(cleanTeacher.getId())
                    .folderPath("it")
                    .canRead(true)
                    .canWrite(true)
                    .canDelete(false)
                    .canManage(false)
                    .isActive(true)
                    .grantedAt(LocalDateTime.now())
                    .build();

            when(folderPermissionRepository.findCoveringPermissions(eq(cleanTeacher.getId()), eq("it/year2/networks"),
                    any()))
                    .thenReturn(List.of(permission));

            assertThat(permissionService.hasReadPermission(cleanTeacher, "it/year2/networks")).isTrue();
        }

        @Test
        @DisplayName("should respect hierarchical permissions")
        void shouldRespectHierarchicalPermissions() {
            User cleanTeacher = User.builder()
                    .id(99L)
                    .role(Role.ROLE_TEACHER.getValue())
                    .assignedDepartments(Collections.emptyList())
                    .build();

            FolderPermission permission = FolderPermission.builder()
                    .userId(cleanTeacher.getId())
                    .folderPath("it")
                    .canRead(true) // Read explicitly granted
                    .canWrite(true)
                    .isActive(true)
                    .grantedAt(LocalDateTime.now())
                    .build();

            when(folderPermissionRepository.findCoveringPermissions(
                    eq(cleanTeacher.getId()), eq("it/year2/section-a/networks"), any()))
                    .thenReturn(List.of(permission));

            // Should rely on FolderPermission, not Department
            assertThat(permissionService.hasReadPermission(cleanTeacher, "it/year2/section-a/networks")).isTrue();
        }

        @Test
        @DisplayName("should respect hierarchical write permissions")
        void shouldRespectHierarchicalWritePermissions() {
            // For WRITE, department access is currently REQUIRED by logic:
            // if (!hasDepartmentAccess...) return false;
            // So we MUST use a user with department access.

            FolderPermission permission = FolderPermission.builder()
                    .userId(teacherWithDepartment.getId())
                    .folderPath("it")
                    .canRead(true)
                    .canWrite(true) // Write explicitly granted
                    .isActive(true)
                    .grantedAt(LocalDateTime.now())
                    .build();

            when(folderPermissionRepository.findCoveringPermissions(
                    eq(teacherWithDepartment.getId()), eq("it/year2/section-a/networks"), any()))
                    .thenReturn(List.of(permission));

            assertThat(permissionService.hasWritePermission(teacherWithDepartment, "it/year2/section-a/networks"))
                    .isTrue();
        }

        @Test
        @DisplayName("should not grant access for expired permissions")
        void shouldNotGrantAccessForExpiredPermissions() {
            User cleanTeacher = User.builder()
                    .id(99L)
                    .role(Role.ROLE_TEACHER.getValue())
                    .assignedDepartments(Collections.emptyList())
                    .build();

            FolderPermission expiredPermission = FolderPermission.builder()
                    .userId(cleanTeacher.getId())
                    .folderPath("it")
                    .canRead(true)
                    .canWrite(true)
                    .isActive(true)
                    .grantedAt(LocalDateTime.now().minusDays(30))
                    .expiresAt(LocalDateTime.now().minusDays(1)) // Expired
                    .build();

            // Return the expired permission to ensure Service checks isValid()
            when(folderPermissionRepository.findCoveringPermissions(eq(cleanTeacher.getId()), any(), any()))
                    .thenReturn(List.of(expiredPermission));

            assertThat(permissionService.hasReadPermission(cleanTeacher, "it/year2")).isFalse();
        }

        @Test
        @DisplayName("should not grant access for inactive permissions")
        void shouldNotGrantAccessForInactivePermissions() {
            User cleanTeacher = User.builder()
                    .id(99L)
                    .role(Role.ROLE_TEACHER.getValue())
                    .assignedDepartments(Collections.emptyList())
                    .build();

            FolderPermission inactivePermission = FolderPermission.builder()
                    .userId(cleanTeacher.getId())
                    .folderPath("it")
                    .canRead(true)
                    .canWrite(true)
                    .isActive(false) // Inactive
                    .grantedAt(LocalDateTime.now())
                    .build();

            when(folderPermissionRepository.findCoveringPermissions(eq(cleanTeacher.getId()), any(), any()))
                    .thenReturn(List.of(inactivePermission));

            assertThat(permissionService.hasReadPermission(cleanTeacher, "it/year2")).isFalse();
        }
    }

    @Nested
    @DisplayName("Permission Assertion")
    class PermissionAssertionTests {

        @Test
        @DisplayName("assertPermission should throw when access denied")
        void assertPermissionShouldThrowWhenDenied() {
            User cleanTeacher = User.builder()
                    .id(99L)
                    .email("clean@test.com")
                    .role(Role.ROLE_TEACHER.getValue())
                    .assignedDepartments(Collections.emptyList())
                    .build();

            // Testing WRITE to a folder.
            // 1. teacher has NO department access -> hasWriteAccess returns false
            // immediately.
            // 2. assertPermission throws.

            assertThatThrownBy(() -> permissionService.assertPermission(cleanTeacher, "it/year1",
                    PermissionService.PermissionType.WRITE))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("permission");
        }

        @Test
        @DisplayName("assertPermission should not throw for admin")
        void assertPermissionShouldNotThrowForAdmin() {
            permissionService.assertPermission(admin, "any/folder", PermissionService.PermissionType.MANAGE);
        }
    }

    @Nested
    @DisplayName("Mid-Session Permission Revocation")
    class MidSessionRevocationTests {

        @Test
        @DisplayName("should deny access after permission is revoked")
        void shouldDenyAccessAfterRevocation() {
            // Testing WRITE, so need department access

            FolderPermission permission = FolderPermission.builder()
                    .userId(teacherWithDepartment.getId())
                    .folderPath("it")
                    .canRead(true)
                    .canWrite(true)
                    .isActive(true)
                    .grantedAt(LocalDateTime.now())
                    .build();

            when(folderPermissionRepository.findCoveringPermissions(eq(teacherWithDepartment.getId()), eq("it/year2"),
                    any()))
                    .thenReturn(List.of(permission));

            assertThat(permissionService.hasWritePermission(teacherWithDepartment, "it/year2")).isTrue();

            // Simulate revocation
            when(folderPermissionRepository.findCoveringPermissions(eq(teacherWithDepartment.getId()), eq("it/year2"),
                    any()))
                    .thenReturn(Collections.emptyList());

            assertThat(permissionService.hasWritePermission(teacherWithDepartment, "it/year2")).isFalse();
        }
    }
}
