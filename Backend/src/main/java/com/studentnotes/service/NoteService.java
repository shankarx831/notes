package com.studentnotes.service;

import com.studentnotes.dto.request.CreateNoteRequest;
import com.studentnotes.dto.request.UpdateNoteRequest;
import com.studentnotes.dto.response.NoteResponse;
import com.studentnotes.exception.AccessDeniedException;
import com.studentnotes.exception.ResourceNotFoundException;
import com.studentnotes.exception.ValidationException;
import com.studentnotes.model.Note;
import com.studentnotes.model.NoteVersion;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.AuditAction;
import com.studentnotes.model.enums.NoteStatus;
import com.studentnotes.repository.NoteRepository;
import com.studentnotes.repository.NoteVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * Service for managing notes with versioning support.
 */
@Service
public class NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteService.class);
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteVersionRepository noteVersionRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private com.studentnotes.repository.DepartmentRepository departmentRepository;

    /**
     * Creates a new {@link Note} entity and persists its initial
     * {@link NoteVersion} snapshot v1.
     * <p>
     * <strong>Transactional Boundary:</strong> This method executes atomically. If
     * the version creation
     * or audit logging fails, the Note record is rolled back to prevent phantom
     * data.
     * </p>
     * <strong>Invariants:</strong>
     * <ul>
     * <li>Content payload must be &lt; 10MB to protect heap memory.</li>
     * <li>User must hold explicit {@code WRITE} permission on the target folder
     * path.</li>
     * <li>The Note status defaults to {@code DRAFT} unless
     * {@code publishImmediately} is explicitly requested.</li>
     * </ul>
     *
     * @param teacher The user attempting the upload (must be authenticated).
     * @param request The data payload containing metadata and content.
     * @return The persisted Note entity with generated ID.
     * @throws ValidationException   if content exceeds size limits.
     * @throws AccessDeniedException if the user lacks folder permissions.
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "publicNoteTree", allEntries = true)
    public Note createNote(User teacher, CreateNoteRequest request) {
        // Validate file size
        if (request.getContent() != null) {
            long contentSize = request.getContent().getBytes(StandardCharsets.UTF_8).length;
            if (contentSize > MAX_FILE_SIZE_BYTES) {
                throw ValidationException.fileTooLarge(MAX_FILE_SIZE_BYTES);
            }
        }

        String folderPath = buildFolderPath(request.getDepartment(), request.getYear(),
                request.getSection(), request.getSubject());

        // Check write permission
        permissionService.assertPermission(teacher, folderPath, PermissionService.PermissionType.WRITE);

        // Create note
        NoteStatus initialStatus = request.isPublishImmediately() ? NoteStatus.PUBLISHED : NoteStatus.DRAFT;

        Note note = Note.builder()
                .title(request.getTitle())
                .department(request.getDepartment())
                .year(request.getYear())
                .section(request.getSection())
                .subject(request.getSubject())
                .content(request.getContent())
                .type("md")
                .currentVersion(1)
                .status(initialStatus)
                .uploadedByUserId(teacher.getId())
                .uploadedByEmail(teacher.getEmail())
                .uploadedByName(teacher.getName())
                .fileSizeBytes((long) request.getContent().getBytes(StandardCharsets.UTF_8).length)
                .createdAt(LocalDateTime.now())
                .build();

        if (initialStatus == NoteStatus.PUBLISHED) {
            note.setPublishedAt(LocalDateTime.now());
        }

        Note savedNote = noteRepository.save(note);

        // Create initial version
        createVersion(savedNote, teacher,
                request.getChangeSummary() != null ? request.getChangeSummary() : "Initial version");

        // Audit log
        auditService.logAction(
                AuditAction.NOTE_CREATED,
                teacher,
                "Note",
                savedNote.getId(),
                String.format("Created note '%s' in %s", savedNote.getTitle(), folderPath));

        log.info("Note {} created by {} in folder {}",
                savedNote.getPublicId(), teacher.getEmail(), folderPath);

        return savedNote;
    }

    /**
     * Updates an existing note and generates a new immutable version.
     * <p>
     * <strong>Concurrency:</strong> This method relies on optimistic locking via
     * JPA versioning (implicit)
     * or explicit status checks. If a {@link ConcurrentModificationException}
     * occurs, the client must retry.
     * </p>
     * <strong>Logic Flow:</strong>
     * <ol>
     * <li>Verifies ownership OR the {@code MANAGE} permission for the note's
     * folder.</li>
     * <li>Validates new content size (if changed).</li>
     * <li>Updates the mutable {@link Note} entity.</li>
     * <li>Snapshots the new state into a new {@link NoteVersion} (v+1).</li>
     * <li>Logs the action to the Audit Trail with the "Before" vs "After"
     * context.</li>
     * </ol>
     *
     * @param teacher      The user attempting the update.
     * @param notePublicId The reliable UUID of the note.
     * @param request      The data fields to patch. Null fields are ignored.
     * @return The updated Note entity.
     * @throws ResourceNotFoundException if the ID is invalid.
     * @throws AccessDeniedException     if the user does not own the note.
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "publicNoteTree", allEntries = true)
    public Note updateNote(User teacher, String notePublicId, UpdateNoteRequest request) {
        Note note = noteRepository.findByPublicId(notePublicId)
                .orElseThrow(() -> ResourceNotFoundException.note(notePublicId));

        // Check ownership or write permission
        if (!isOwner(teacher, note)) {
            String folderPath = note.getFolderPath();
            permissionService.assertPermission(teacher, folderPath, PermissionService.PermissionType.MANAGE);
        }

        // Store previous state for audit
        String previousTitle = note.getTitle();

        // Update fields
        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            // Validate size
            long contentSize = request.getContent().getBytes(StandardCharsets.UTF_8).length;
            if (contentSize > MAX_FILE_SIZE_BYTES) {
                throw ValidationException.fileTooLarge(MAX_FILE_SIZE_BYTES);
            }
            note.setContent(request.getContent());
            note.setFileSizeBytes(contentSize);
        }
        if (request.getDepartment() != null) {
            note.setDepartment(request.getDepartment());
        }
        if (request.getYear() != null) {
            note.setYear(request.getYear());
        }
        if (request.getSection() != null) {
            note.setSection(request.getSection());
        }
        if (request.getSubject() != null) {
            note.setSubject(request.getSubject());
        }

        // Increment version
        note.setCurrentVersion(note.getCurrentVersion() + 1);
        note.setUpdatedAt(LocalDateTime.now());

        Note savedNote = noteRepository.save(note);

        // Create new version
        createVersion(savedNote, teacher, request.getChangeSummary());

        // Audit log
        auditService.logAction(
                AuditAction.NOTE_UPDATED,
                teacher,
                "Note",
                savedNote.getId(),
                String.format("Updated note '%s' to version %d: %s",
                        previousTitle, savedNote.getCurrentVersion(), request.getChangeSummary()),
                previousTitle,
                savedNote.getTitle());

        log.info("Note {} updated by {} - version {}",
                notePublicId, teacher.getEmail(), savedNote.getCurrentVersion());

        return savedNote;
    }

    /**
     * Publishes a draft note.
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "publicNoteTree", allEntries = true)
    public Note publishNote(User user, String notePublicId) {
        Note note = noteRepository.findByPublicId(notePublicId)
                .orElseThrow(() -> ResourceNotFoundException.note(notePublicId));

        // Check permission
        if (!isOwner(user, note) && !user.isAdmin()) {
            throw AccessDeniedException.notResourceOwner();
        }

        if (note.getStatus() != NoteStatus.DRAFT) {
            throw new com.studentnotes.exception.BusinessRuleViolationException(
                    "Only draft notes can be published. Current status: " + note.getStatus(),
                    "INVALID_NOTE_STATUS");
        }

        note.transitionTo(NoteStatus.PUBLISHED);
        Note savedNote = noteRepository.save(note);

        // Audit log
        auditService.logAction(
                AuditAction.NOTE_PUBLISHED,
                user,
                "Note",
                savedNote.getId(),
                String.format("Published note '%s'", savedNote.getTitle()));

        log.info("Note {} published by {}", notePublicId, user.getEmail());

        return savedNote;
    }

    /**
     * Finds a note by public ID.
     */
    @Transactional(readOnly = true)
    public Note findByPublicId(String publicId) {
        return noteRepository.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.note(publicId));
    }

    /**
     * Finds notes by teacher with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Note> findByTeacher(Long teacherId, Pageable pageable) {
        return noteRepository.findByUploadedByUserIdOrderByUpdatedAtDesc(teacherId, pageable);
    }

    /**
     * Finds notes by teacher and status.
     */
    @Transactional(readOnly = true)
    public Page<Note> findByTeacherAndStatus(Long teacherId, NoteStatus status, Pageable pageable) {
        return noteRepository.findByUploadedByUserIdAndStatus(teacherId, status, pageable);
    }

    /**
     * Gets version history for a note.
     */
    @Transactional(readOnly = true)
    public List<NoteVersion> getVersionHistory(String notePublicId) {
        Note note = findByPublicId(notePublicId);
        return noteVersionRepository.findByNoteIdOrderByVersionNumberDesc(note.getId());
    }

    /**
     * Gets a specific version of a note.
     */
    @Transactional(readOnly = true)
    public NoteVersion getVersion(String notePublicId, int versionNumber) {
        Note note = findByPublicId(notePublicId);
        return noteVersionRepository.findByNoteIdAndVersionNumber(note.getId(), versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("NoteVersion",
                        notePublicId + "/v" + versionNumber));
    }

    /**
     * Converts a Note entity to response DTO.
     */
    public NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .publicId(note.getPublicId())
                .title(note.getTitle())
                .department(note.getDepartment())
                .year(note.getYear())
                .section(note.getSection())
                .subject(note.getSubject())
                .folderPath(note.getFolderPath())
                .content(note.getContent())
                .type(note.getType())
                .currentVersion(note.getCurrentVersion())
                .status(note.getStatus())
                .statusDescription(note.getStatus().getDescription())
                .uploadedBy(NoteResponse.UploaderInfoDto.builder()
                        .name(note.getUploadedByName())
                        .email(note.getUploadedByEmail())
                        .build())
                .likes(note.getLikes())
                .dislikes(note.getDislikes())
                .fileSizeBytes(note.getFileSizeBytes())
                .mimeType(note.getMimeType())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .publishedAt(note.getPublishedAt())
                .build();
    }

    /**
     * Converts a Note entity to response DTO (without content - for list views).
     */
    public NoteResponse toListResponse(Note note) {
        return NoteResponse.builder()
                .publicId(note.getPublicId())
                .title(note.getTitle())
                .department(note.getDepartment())
                .year(note.getYear())
                .section(note.getSection())
                .subject(note.getSubject())
                .folderPath(note.getFolderPath())
                .type(note.getType())
                .currentVersion(note.getCurrentVersion())
                .status(note.getStatus())
                .statusDescription(note.getStatus().getDescription())
                .uploadedBy(NoteResponse.UploaderInfoDto.builder()
                        .name(note.getUploadedByName())
                        .email(note.getUploadedByEmail())
                        .build())
                .likes(note.getLikes())
                .dislikes(note.getDislikes())
                .fileSizeBytes(note.getFileSizeBytes())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .publishedAt(note.getPublishedAt())
                .build();
    }

    // ==================== Private Helper Methods ====================

    private void createVersion(Note note, User createdBy, String changeSummary) {
        // Clear current version flag on existing versions
        noteVersionRepository.clearCurrentVersion(note.getId());

        // Calculate content hash
        String contentHash = null;
        if (note.getContent() != null) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(note.getContent().getBytes(StandardCharsets.UTF_8));
                contentHash = HexFormat.of().formatHex(hash);
            } catch (Exception e) {
                log.warn("Failed to compute content hash: {}", e.getMessage());
            }
        }

        NoteVersion version = NoteVersion.builder()
                .noteId(note.getId())
                .versionNumber(note.getCurrentVersion())
                .title(note.getTitle())
                .content(note.getContent())
                .fileSizeBytes(note.getFileSizeBytes())
                .mimeType(note.getMimeType())
                .contentHash(contentHash)
                .createdByUserId(createdBy.getId())
                .createdByEmail(createdBy.getEmail())
                .createdAt(LocalDateTime.now())
                .changeSummary(changeSummary)
                .isCurrentVersion(true)
                .build();

        noteVersionRepository.save(version);
    }

    private boolean isOwner(User user, Note note) {
        return note.getUploadedByUserId() != null &&
                note.getUploadedByUserId().equals(user.getId());
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "publicNoteTree")
    @SuppressWarnings("unchecked")
    public java.util.Map<String, Object> getPublicNoteTree() {
        // Fetch only enabled notes
        List<Note> notes = noteRepository.findByEnabledTrue();

        // Root Tree Map
        java.util.Map<String, Object> tree = new java.util.HashMap<>();

        // 1. Pre-fill with all managed departments
        for (com.studentnotes.model.Department d : departmentRepository.findAll()) {
            tree.put(d.getName(), new java.util.HashMap<String, Object>());
        }

        for (Note n : notes) {
            // 2. Get or Create Department Map
            java.util.Map<String, Object> deptMap = (java.util.Map<String, Object>) tree.computeIfAbsent(
                    n.getDepartment(),
                    k -> new java.util.HashMap<>());

            // 3. Get or Create Year Map
            java.util.Map<String, Object> yearMap = (java.util.Map<String, Object>) deptMap.computeIfAbsent(n.getYear(),
                    k -> new java.util.HashMap<>());

            // 4. Get or Create Section Map
            java.util.Map<String, Object> sectionMap = (java.util.Map<String, Object>) yearMap.computeIfAbsent(
                    n.getSection(),
                    k -> new java.util.HashMap<>());

            // 5. Get or Create Subject List
            List<java.util.Map<String, Object>> subjectList = (List<java.util.Map<String, Object>>) sectionMap
                    .computeIfAbsent(n.getSubject(), k -> new java.util.ArrayList<>());

            // 6. Create Note Object
            java.util.Map<String, Object> noteData = new java.util.HashMap<>();
            noteData.put("id", n.getPublicId()); // Use Public ID for security
            noteData.put("type", "md");
            noteData.put("content", n.getContent());

            java.util.Map<String, Object> meta = new java.util.HashMap<>();
            meta.put("title", n.getTitle());
            meta.put("order", 999);
            meta.put("likes", n.getLikes());
            meta.put("dislikes", n.getDislikes());
            meta.put("uploadedBy", n.getUploadedByName());
            meta.put("uploadedByEmail", n.getUploadedByEmail());
            meta.put("createdAt", n.getCreatedAt());

            noteData.put("meta", meta);

            // Add to list
            subjectList.add(noteData);
        }
        return tree;
    }

    private String buildFolderPath(String department, String year, String section, String subject) {
        StringBuilder path = new StringBuilder(department);
        if (year != null) {
            path.append("/").append(year);
        }
        if (section != null) {
            path.append("/").append(section);
        }
        if (subject != null) {
            path.append("/").append(subject);
        }
        return path.toString();
    }

}
