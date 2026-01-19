package com.studentnotes.dto.response;

import com.studentnotes.model.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for audit log entries in API responses.
 * Read-only view of audit trail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private String correlationId;

    // Actor information
    private ActorInfoDto actor;

    // Action details
    private AuditAction action;
    private String actionDescription;

    // Target information
    private String targetType;
    private String targetPublicId;
    private String targetDescription;

    // Description
    private String description;

    // State changes (optional, for detailed views)
    private String previousState;
    private String newState;

    // Request context
    private String ipAddress;

    // Timestamp
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorInfoDto {
        private String publicId;
        private String email;
        private String role;
    }
}
