package com.studentnotes.controller;

import com.studentnotes.model.*;
import com.studentnotes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private DeletionRequestRepository deletionRequestRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // --- 1. MANAGE DEPARTMENTS ---

    @GetMapping("/departments")
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @PostMapping("/add-department")
    public ResponseEntity<?> addDepartment(@RequestBody Department dept) {
        if (departmentRepository.findByName(dept.getName()).isPresent()) {
            return ResponseEntity.badRequest().body("Department already exists");
        }
        departmentRepository.save(dept);
        return ResponseEntity.ok("Department Added");
    }

    @DeleteMapping("/delete-department/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok("Department Deleted");
    }

    // --- 2. MANAGE TEACHERS ---

    @GetMapping("/teachers")
    public List<User> getTeachers() {
        return userRepository.findByRole("ROLE_TEACHER");
    }

    @PostMapping("/add-teacher")
    public ResponseEntity<?> addTeacher(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        user.setRole("ROLE_TEACHER");
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Securely hash!

        // Ensure defaults if missing
        if (user.getAssignedDepartments() == null)
            user.setAssignedDepartments(List.of());

        userRepository.save(user);
        return ResponseEntity.ok("Teacher Added");
    }

    @PutMapping("/toggle-user/{id}")
    public ResponseEntity<?> toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok("User status updated");
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // Option A: Just delete them.
        // Option B: Better, delete their notes too or reassign them.
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

    // --- 2. DELETION REQUESTS (FOR NOTES) ---

    @GetMapping("/deletion-requests")
    public List<DeletionRequest> getRequests() {
        return deletionRequestRepository.findByStatus(DeletionRequest.Status.PENDING);
    }

    @PostMapping("/approve-delete/{requestId}")
    public ResponseEntity<?> approveDelete(@PathVariable Long requestId) {
        DeletionRequest request = deletionRequestRepository.findById(requestId).orElseThrow();
        Note note = request.getNote();
        note.setEnabled(false);
        noteRepository.save(note);

        request.setStatus(DeletionRequest.Status.APPROVED);
        deletionRequestRepository.save(request);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/reject-delete/{requestId}")
    public ResponseEntity<?> rejectDelete(@PathVariable Long requestId) {
        DeletionRequest request = deletionRequestRepository.findById(requestId).orElseThrow();
        request.setStatus(DeletionRequest.Status.REJECTED);
        deletionRequestRepository.save(request);
        return ResponseEntity.ok("Rejected");
    }
}