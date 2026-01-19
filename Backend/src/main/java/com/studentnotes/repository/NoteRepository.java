package com.studentnotes.repository;

import com.studentnotes.model.Note;
import com.studentnotes.model.enums.NoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // ==================== Find by Public ID ====================

    Optional<Note> findByPublicId(String publicId);

    // ==================== Status-based queries ====================

    List<Note> findByStatus(NoteStatus status);

    Page<Note> findByStatus(NoteStatus status, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.status IN :statuses")
    Page<Note> findByStatusIn(@Param("statuses") List<NoteStatus> statuses, Pageable pageable);

    // ==================== Teacher's notes ====================

    Page<Note> findByUploadedByUserId(Long userId, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.uploadedByUserId = :userId AND n.status = :status")
    Page<Note> findByUploadedByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") NoteStatus status,
            Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.uploadedByUserId = :userId ORDER BY n.updatedAt DESC NULLS LAST, n.createdAt DESC")
    Page<Note> findByUploadedByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // ==================== Folder/hierarchy queries ====================

    List<Note> findByDepartmentAndYearAndSubject(String department, String year, String subject);

    @Query("SELECT n FROM Note n WHERE n.department = :dept AND n.status = :status")
    Page<Note> findByDepartmentAndStatus(
            @Param("dept") String department,
            @Param("status") NoteStatus status,
            Pageable pageable);

    @Query("SELECT n FROM Note n WHERE " +
            "(:department IS NULL OR n.department = :department) AND " +
            "(:year IS NULL OR n.year = :year) AND " +
            "(:section IS NULL OR n.section = :section) AND " +
            "(:subject IS NULL OR n.subject = :subject) AND " +
            "(:status IS NULL OR n.status = :status)")
    Page<Note> findByFolderPath(
            @Param("department") String department,
            @Param("year") String year,
            @Param("section") String section,
            @Param("subject") String subject,
            @Param("status") NoteStatus status,
            Pageable pageable);

    // ==================== Count queries for dashboard ====================

    long countByStatus(NoteStatus status);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.createdAt >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.createdAt >= :since AND n.status = :status")
    long countCreatedSinceAndStatus(@Param("since") LocalDateTime since, @Param("status") NoteStatus status);

    long countByUploadedByUserId(Long userId);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.uploadedByUserId = :userId AND n.status = :status")
    long countByUploadedByUserIdAndStatus(@Param("userId") Long userId, @Param("status") NoteStatus status);

    // ==================== Search queries ====================

    @Query("SELECT n FROM Note n WHERE " +
            "n.status = :status AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Note> searchByTitleOrContent(
            @Param("query") String query,
            @Param("status") NoteStatus status,
            Pageable pageable);

    // ==================== Legacy compatibility ====================

    @Query("SELECT n FROM Note n WHERE n.status NOT IN ('DELETED', 'ARCHIVED')")
    List<Note> findByEnabledTrue();

    @Query("SELECT n FROM Note n WHERE n.status NOT IN ('DELETED', 'ARCHIVED')")
    Page<Note> findAllActive(Pageable pageable);
}