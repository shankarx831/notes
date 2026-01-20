package com.studentnotes.controller;

import com.studentnotes.model.Note;
import com.studentnotes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequestMapping("/api/public")
@org.springframework.web.bind.annotation.CrossOrigin(origins = "http://localhost:5173")
public class PublicController {

    @Autowired
    private com.studentnotes.service.NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private com.studentnotes.repository.DepartmentRepository departmentRepository;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/departments")
    public List<com.studentnotes.model.Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @GetMapping("/tree")
    public Map<String, Object> getNotesTree() {
        return noteService.getPublicNoteTree();
    }

    @org.springframework.web.bind.annotation.PostMapping("/notes/{id}/like")
    public ResponseEntity<?> likeNote(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return noteRepository.findById(id).map(n -> {
            n.setLikes(n.getLikes() + 1);
            noteRepository.save(n);
            return ResponseEntity.ok(Map.of("likes", n.getLikes()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @org.springframework.web.bind.annotation.PostMapping("/notes/{id}/dislike")
    public ResponseEntity<?> dislikeNote(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return noteRepository.findById(id).map(n -> {
            n.setDislikes(n.getDislikes() + 1);
            noteRepository.save(n);
            return ResponseEntity.ok(Map.of("dislikes", n.getDislikes()));
        }).orElse(ResponseEntity.notFound().build());
    }
}