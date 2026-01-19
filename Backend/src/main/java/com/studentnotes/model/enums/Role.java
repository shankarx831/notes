package com.studentnotes.model.enums;

/**
 * User roles in the system.
 * Replaces hardcoded role strings.
 */
public enum Role {
    ROLE_ADMIN("Administrator with full system access"),
    ROLE_TEACHER("Teacher who can upload and manage notes"),
    ROLE_STUDENT("Student who can view notes");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return this.name();
    }

    /**
     * Returns role without ROLE_ prefix for display purposes
     */
    public String getDisplayName() {
        return this.name().replace("ROLE_", "");
    }
}
