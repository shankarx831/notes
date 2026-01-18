package com.studentnotes.repository;

import com.studentnotes.model.DeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, Long> {
    
    // Find requests by status (PENDING, APPROVED, REJECTED)
    List<DeletionRequest> findByStatus(DeletionRequest.Status status);
    
    // Find requests made by a specific teacher
    List<DeletionRequest> findByTeacherId(Long teacherId);
}