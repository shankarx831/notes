package com.studentnotes.repository;

import com.studentnotes.model.User;
import com.studentnotes.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ==================== Find by unique identifiers ====================

    Optional<User> findByEmail(String email);

    Optional<User> findByPublicId(String publicId);

    // ==================== Role-based queries ====================

    List<User> findByRole(String role);

    Page<User> findByRole(String role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status")
    Page<User> findByRoleAndStatus(
            @Param("role") String role,
            @Param("status") UserStatus status,
            Pageable pageable);

    // ==================== Status-based queries ====================

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status IN :statuses")
    Page<User> findByStatusIn(@Param("statuses") List<UserStatus> statuses, Pageable pageable);

    // ==================== Count queries for dashboard ====================

    long countByRole(String role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.status = :status")
    long countByRoleAndStatus(@Param("role") String role, @Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);

    // ==================== Search queries ====================

    @Query("SELECT u FROM User u WHERE " +
            "u.role = :role AND " +
            "(LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchByNameOrEmail(
            @Param("role") String role,
            @Param("query") String query,
            Pageable pageable);

    // ==================== Department access queries ====================

    @Query("SELECT u FROM User u JOIN u.assignedDepartments d WHERE d = :department")
    List<User> findByAssignedDepartment(@Param("department") String department);

    // ==================== Validation queries ====================

    boolean existsByEmail(String email);
}