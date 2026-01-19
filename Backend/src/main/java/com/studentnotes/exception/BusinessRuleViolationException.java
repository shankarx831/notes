package com.studentnotes.exception;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessRuleViolationException extends ApplicationException {

    public BusinessRuleViolationException(String message, String errorCode) {
        super(message, errorCode, 409); // Conflict
    }

    public static BusinessRuleViolationException duplicateDeletionRequest(String notePublicId) {
        return new BusinessRuleViolationException(
                "A pending deletion request already exists for this note",
                "DUPLICATE_DELETION_REQUEST");
    }

    public static BusinessRuleViolationException alreadyResolved() {
        return new BusinessRuleViolationException(
                "This request has already been resolved",
                "ALREADY_RESOLVED");
    }

    public static BusinessRuleViolationException invalidStateTransition(String from, String to) {
        return new BusinessRuleViolationException(
                String.format("Cannot transition from %s to %s", from, to),
                "INVALID_STATE_TRANSITION");
    }

    public static BusinessRuleViolationException emailAlreadyExists(String email) {
        return new BusinessRuleViolationException(
                String.format("Email already exists: %s", email),
                "EMAIL_ALREADY_EXISTS");
    }

    public static BusinessRuleViolationException cannotDeleteSelf() {
        return new BusinessRuleViolationException(
                "You cannot disable your own account",
                "CANNOT_DISABLE_SELF");
    }
}
