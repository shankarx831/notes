package com.studentnotes.model.enums;

/**
 * Explicit state machine for Note lifecycle.
 * No boolean flags - clear state transitions only.
 * 
 * State Transitions:
 * DRAFT -> PUBLISHED (teacher publishes)
 * PUBLISHED -> DELETE_PENDING (teacher requests deletion)
 * DELETE_PENDING -> DELETED (admin approves)
 * DELETE_PENDING -> PUBLISHED (admin rejects deletion)
 * Any state -> ARCHIVED (admin archives)
 */
public enum NoteStatus {
    DRAFT("Draft - not visible to students"),
    PUBLISHED("Published - visible to students"),
    DELETE_PENDING("Deletion requested - awaiting admin approval"),
    DELETED("Soft deleted - not visible, can be restored"),
    ARCHIVED("Archived - not visible, read-only");

    private final String description;

    NoteStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Validates if a state transition is allowed
     */
    public boolean canTransitionTo(NoteStatus targetStatus) {
        return switch (this) {
            case DRAFT -> targetStatus == PUBLISHED || targetStatus == ARCHIVED;
            case PUBLISHED -> targetStatus == DELETE_PENDING || targetStatus == ARCHIVED;
            case DELETE_PENDING -> targetStatus == DELETED || targetStatus == PUBLISHED;
            case DELETED -> targetStatus == ARCHIVED; // Can only archive deleted notes
            case ARCHIVED -> false; // Terminal state
        };
    }
}
