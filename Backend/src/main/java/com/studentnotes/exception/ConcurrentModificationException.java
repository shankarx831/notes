package com.studentnotes.exception;

/**
 * Exception thrown for concurrent modification conflicts.
 */
public class ConcurrentModificationException extends ApplicationException {

    public ConcurrentModificationException(String message) {
        super(message, "CONCURRENT_MODIFICATION", 409);
    }

    public static ConcurrentModificationException optimisticLock(String resourceType) {
        return new ConcurrentModificationException(
                String.format("%s was modified by another request. Please refresh and try again.", resourceType));
    }
}
