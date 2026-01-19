package com.studentnotes.repository;

import com.studentnotes.model.FolderPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FolderPermissionRepository extends JpaRepository<FolderPermission, Long> {

    // ==================== User permission queries ====================

    List<FolderPermission> findByUserId(Long userId);

    @Query("SELECT fp FROM FolderPermission fp WHERE fp.userId = :userId AND fp.isActive = true " +
            "AND (fp.expiresAt IS NULL OR fp.expiresAt > :now)")
    List<FolderPermission> findActivePermissions(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    Optional<FolderPermission> findByUserIdAndFolderPath(Long userId, String folderPath);

    // ==================== Permission checking ====================

    @Query("SELECT fp FROM FolderPermission fp WHERE " +
            "fp.userId = :userId AND " +
            "fp.isActive = true AND " +
            "(fp.expiresAt IS NULL OR fp.expiresAt > :now) AND " +
            ":targetPath LIKE CONCAT(fp.folderPath, '%')")
    List<FolderPermission> findCoveringPermissions(
            @Param("userId") Long userId,
            @Param("targetPath") String targetPath,
            @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(fp) > 0 FROM FolderPermission fp WHERE " +
            "fp.userId = :userId AND " +
            "fp.isActive = true AND " +
            "(fp.expiresAt IS NULL OR fp.expiresAt > :now) AND " +
            "fp.canWrite = true AND " +
            ":targetPath LIKE CONCAT(fp.folderPath, '%')")
    boolean hasWritePermission(
            @Param("userId") Long userId,
            @Param("targetPath") String targetPath,
            @Param("now") LocalDateTime now);

    // ==================== Cleanup queries ====================

    @Query("SELECT fp FROM FolderPermission fp WHERE fp.expiresAt IS NOT NULL AND fp.expiresAt <= :now")
    List<FolderPermission> findExpired(@Param("now") LocalDateTime now);
}
