package com.studentnotes.model.enums;

/**
 * Status enum for deletion requests.
 * Once resolved (APPROVED/REJECTED), requests are immutable.
 */
public enum DeletionRequestStatus {
    PENDING("Awaiting admin review"),
    APPROVED("Deletion approved by admin"),
    REJECTED("Deletion rejected by admin");

    private final String description;

    DeletionRequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isResolved() {
        return this == APPROVED || this == REJECTED;
    }
}
