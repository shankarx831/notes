package com.studentnotes.repository;

import com.studentnotes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    // Finds all notes where enabled = true (Active notes)
    List<Note> findByEnabledTrue();
    
    // Optional: Find by specific hierarchy if needed later
    List<Note> findByDepartmentAndYearAndSubject(String department, String year, String subject);
}