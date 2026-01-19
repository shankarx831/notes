package com.studentnotes.model.enums;

/**
 * User account status - explicit states instead of boolean enabled flag.
 */
public enum UserStatus {
    ACTIVE("Account is active and can access the system"),
    DISABLED("Account is disabled - cannot login"),
    PENDING_VERIFICATION("Account pending email verification"),
    SUSPENDED("Account temporarily suspended by admin");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }
}
