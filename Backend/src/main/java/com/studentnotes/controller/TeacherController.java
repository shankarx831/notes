package com.studentnotes.controller;

import com.studentnotes.model.Note;
import com.studentnotes.model.User;
import com.studentnotes.model.DeletionRequest;
import com.studentnotes.repository.NoteRepository;
import com.studentnotes.repository.UserRepository;
import com.studentnotes.repository.DeletionRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "http://localhost:5173")
public class TeacherController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private DeletionRequestRepository deletionRequestRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Upload a new note (Teacher)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadNote(@RequestBody Note note) {
        // Find teacher to set metadata
        User teacher = userRepository.findById(note.getUploadedByUserId()).orElseThrow();
        note.setUploadedByEmail(teacher.getEmail());
        note.setUploadedByName(teacher.getName());

        note.setEnabled(true);
        Note savedNote = noteRepository.save(note);
        return ResponseEntity.ok(savedNote);
    }

    /**
     * Edit an existing note
     */
    @PutMapping("/notes/{id}")
    public ResponseEntity<?> editNote(@PathVariable Long id, @RequestBody Note noteDetails) {
        Note note = noteRepository.findById(id).orElseThrow();

        // Update fields
        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        note.setDepartment(noteDetails.getDepartment());
        note.setYear(noteDetails.getYear());
        note.setSection(noteDetails.getSection());
        note.setSubject(noteDetails.getSubject());

        noteRepository.save(note);
        return ResponseEntity.ok("Note updated");
    }

    /**
     * Teacher Dashboard: Get their notes and requests
     */
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<?> getDashboard(@PathVariable Long userId) {
        List<Note> myNotes = noteRepository.findAll().stream()
                .filter(n -> n.getUploadedByUserId() != null && n.getUploadedByUserId().equals(userId))
                .toList();

        List<DeletionRequest> myRequests = deletionRequestRepository.findByTeacherId(userId);

        return ResponseEntity.ok(Map.of(
                "notes", myNotes,
                "requests", myRequests));
    }

    /**
     * Request admin approval to delete a note
     */
    @PostMapping("/request-delete")
    public ResponseEntity<?> requestDelete(@RequestBody Map<String, Object> payload) {

        Long noteId = Long.parseLong(payload.get("noteId").toString());
        String reason = (String) payload.get("reason");
        String teacherEmail = (String) payload.get("email");

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        DeletionRequest request = new DeletionRequest();
        request.setNote(note);
        request.setTeacher(teacher);
        request.setReason(reason);
        request.setStatus(DeletionRequest.Status.PENDING);
        request.setRequestedAt(LocalDateTime.now());

        deletionRequestRepository.save(request);

        return ResponseEntity.ok("Deletion request sent");
    }
}
