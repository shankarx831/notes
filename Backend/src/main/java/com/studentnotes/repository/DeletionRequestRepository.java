package com.studentnotes.repository;

import com.studentnotes.model.DeletionRequest;
import com.studentnotes.model.enums.DeletionRequestStatus;
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
public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, Long> {

    // ==================== Find by Public ID ====================

    Optional<DeletionRequest> findByPublicId(String publicId);

    // ==================== Status-based queries ====================

    List<DeletionRequest> findByStatus(DeletionRequestStatus status);

    Page<DeletionRequest> findByStatus(DeletionRequestStatus status, Pageable pageable);

    @Query("SELECT dr FROM DeletionRequest dr WHERE dr.status IN :statuses ORDER BY dr.requestedAt DESC")
    Page<DeletionRequest> findByStatusIn(@Param("statuses") List<DeletionRequestStatus> statuses, Pageable pageable);

    // ==================== Teacher's requests ====================

    List<DeletionRequest> findByTeacherId(Long teacherId);

    Page<DeletionRequest> findByTeacherId(Long teacherId, Pageable pageable);

    @Query("SELECT dr FROM DeletionRequest dr WHERE dr.teacher.id = :teacherId ORDER BY dr.requestedAt DESC")
    Page<DeletionRequest> findByTeacherIdOrderByRequestedAtDesc(@Param("teacherId") Long teacherId, Pageable pageable);

    // ==================== Note-based queries ====================

    @Query("SELECT dr FROM DeletionRequest dr WHERE dr.note.id = :noteId AND dr.status = :status")
    Optional<DeletionRequest> findByNoteIdAndStatus(@Param("noteId") Long noteId,
            @Param("status") DeletionRequestStatus status);

    @Query("SELECT dr FROM DeletionRequest dr WHERE dr.note.publicId = :notePublicId AND dr.status = :status")
    Optional<DeletionRequest> findByNotePublicIdAndStatus(
            @Param("notePublicId") String notePublicId,
            @Param("status") DeletionRequestStatus status);

    @Query("SELECT COUNT(dr) > 0 FROM DeletionRequest dr WHERE dr.note.id = :noteId AND dr.status = 'PENDING'")
    boolean existsPendingRequestForNote(@Param("noteId") Long noteId);

    // ==================== Count queries for dashboard ====================

    long countByStatus(DeletionRequestStatus status);

    @Query("SELECT COUNT(dr) FROM DeletionRequest dr WHERE dr.requestedAt >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(dr) FROM DeletionRequest dr WHERE dr.requestedAt >= :since AND dr.status = :status")
    long countCreatedSinceAndStatus(@Param("since") LocalDateTime since, @Param("status") DeletionRequestStatus status);

    // ==================== Filtering queries ====================

    @Query("SELECT dr FROM DeletionRequest dr WHERE " +
            "(:status IS NULL OR dr.status = :status) AND " +
            "(:teacherId IS NULL OR dr.teacher.id = :teacherId) AND " +
            "(:fromDate IS NULL OR dr.requestedAt >= :fromDate) AND " +
            "(:toDate IS NULL OR dr.requestedAt <= :toDate) " +
            "ORDER BY dr.requestedAt DESC")
    Page<DeletionRequest> findByFilters(
            @Param("status") DeletionRequestStatus status,
            @Param("teacherId") Long teacherId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    // ==================== Legacy compatibility ====================

    @Query("SELECT dr FROM DeletionRequest dr WHERE dr.status = 'PENDING'")
    List<DeletionRequest> findPending();
}