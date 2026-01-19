package com.studentnotes.repository;

import com.studentnotes.model.NoteVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {

    // ==================== Version queries ====================

    List<NoteVersion> findByNoteIdOrderByVersionNumberDesc(Long noteId);

    Page<NoteVersion> findByNoteId(Long noteId, Pageable pageable);

    Optional<NoteVersion> findByNoteIdAndVersionNumber(Long noteId, Integer versionNumber);

    Optional<NoteVersion> findByNoteIdAndIsCurrentVersionTrue(Long noteId);

    // ==================== Version number management ====================

    @Query("SELECT MAX(nv.versionNumber) FROM NoteVersion nv WHERE nv.noteId = :noteId")
    Optional<Integer> findMaxVersionNumber(@Param("noteId") Long noteId);

    @Query("SELECT COUNT(nv) FROM NoteVersion nv WHERE nv.noteId = :noteId")
    long countByNoteId(@Param("noteId") Long noteId);

    // ==================== Current version management ====================

    @Modifying
    @Query("UPDATE NoteVersion nv SET nv.isCurrentVersion = false WHERE nv.noteId = :noteId")
    void clearCurrentVersion(@Param("noteId") Long noteId);
}
