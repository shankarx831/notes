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
    @SuppressWarnings("unchecked")
    public Map<String, Object> getNotesTree() {
        // Fetch only enabled notes
        List<Note> notes = noteRepository.findByEnabledTrue();

        // Root Tree Map
        Map<String, Object> tree = new HashMap<>();

        // 1. Pre-fill with all managed departments
        for (com.studentnotes.model.Department d : departmentRepository.findAll()) {
            tree.put(d.getName(), new HashMap<String, Object>());
        }

        for (Note n : notes) {
            // 2. Get or Create Department Map (using computeIfAbsent in case a note has an
            // unmanaged dept)
            Map<String, Object> deptMap = (Map<String, Object>) tree.computeIfAbsent(n.getDepartment(),
                    k -> new HashMap<>());

            // 2. Get or Create Year Map
            Map<String, Object> yearMap = (Map<String, Object>) deptMap.computeIfAbsent(n.getYear(),
                    k -> new HashMap<>());

            // 3. Get or Create Section Map
            Map<String, Object> sectionMap = (Map<String, Object>) yearMap.computeIfAbsent(n.getSection(),
                    k -> new HashMap<>());

            // 4. Get or Create Subject List
            List<Map<String, Object>> subjectList = (List<Map<String, Object>>) sectionMap
                    .computeIfAbsent(n.getSubject(), k -> new ArrayList<>());

            // 5. Create Note Object
            Map<String, Object> noteData = new HashMap<>();
            noteData.put("id", n.getId().toString());
            noteData.put("type", "md");
            noteData.put("content", n.getContent());

            Map<String, Object> meta = new HashMap<>();
            meta.put("title", n.getTitle());
            meta.put("order", 999);
            meta.put("likes", n.getLikes());
            meta.put("dislikes", n.getDislikes());
            noteData.put("meta", meta);

            // Add to list
            subjectList.add(noteData);
        }
        return tree;
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