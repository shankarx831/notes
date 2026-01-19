package com.studentnotes.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(
                String.format("%s not found: %s", resourceType, identifier),
                "RESOURCE_NOT_FOUND",
                404);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        this(resourceType, String.valueOf(id));
    }

    public static ResourceNotFoundException note(String publicId) {
        return new ResourceNotFoundException("Note", publicId);
    }

    public static ResourceNotFoundException user(String publicId) {
        return new ResourceNotFoundException("User", publicId);
    }

    public static ResourceNotFoundException deletionRequest(String publicId) {
        return new ResourceNotFoundException("DeletionRequest", publicId);
    }

    public static ResourceNotFoundException forId(String resourceType, Long id) {
        return new ResourceNotFoundException(resourceType, id);
    }
}
