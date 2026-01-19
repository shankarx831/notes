package com.studentnotes.model.enums;

/**
 * All auditable actions in the system.
 * Used for AuditLog entries.
 */
public enum AuditAction {
    // Note actions
    NOTE_CREATED("Note was created"),
    NOTE_UPDATED("Note content was updated"),
    NOTE_PUBLISHED("Note was published"),
    NOTE_ARCHIVED("Note was archived"),
    NOTE_DELETED("Note was soft-deleted"),
    NOTE_RESTORED("Note was restored from deleted state"),

    // Deletion request actions
    DELETION_REQUESTED("Deletion was requested"),
    DELETION_APPROVED("Deletion request was approved"),
    DELETION_REJECTED("Deletion request was rejected"),

    // User management actions
    USER_CREATED("User account was created"),
    USER_DISABLED("User account was disabled"),
    USER_ENABLED("User account was enabled"),
    USER_PERMISSIONS_UPDATED("User permissions were updated"),

    // Authentication actions
    USER_LOGIN("User logged in"),
    USER_LOGOUT("User logged out"),
    USER_LOGIN_FAILED("Login attempt failed"),

    // Department/Folder actions
    DEPARTMENT_CREATED("Department was created"),
    DEPARTMENT_DELETED("Department was deleted"),
    FOLDER_PERMISSION_GRANTED("Folder permission was granted"),
    FOLDER_PERMISSION_REVOKED("Folder permission was revoked");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
