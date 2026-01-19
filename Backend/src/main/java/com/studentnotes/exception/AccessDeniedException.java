package com.studentnotes.exception;

/**
 * Exception thrown when access to a resource is denied.
 */
public class AccessDeniedException extends ApplicationException {

    public AccessDeniedException(String message) {
        super(message, "ACCESS_DENIED", 403);
    }

    public AccessDeniedException(String message, String errorCode) {
        super(message, errorCode, 403);
    }

    public static AccessDeniedException noFolderPermission(String folderPath) {
        return new AccessDeniedException(
                String.format("You do not have permission to access folder: %s", folderPath),
                "NO_FOLDER_PERMISSION");
    }

    public static AccessDeniedException notResourceOwner() {
        return new AccessDeniedException(
                "You can only access resources you own",
                "NOT_RESOURCE_OWNER");
    }

    public static AccessDeniedException insufficientRole(String requiredRole) {
        return new AccessDeniedException(
                String.format("This action requires %s role", requiredRole),
                "INSUFFICIENT_ROLE");
    }

    public static AccessDeniedException accountDisabled() {
        return new AccessDeniedException(
                "Your account has been disabled",
                "ACCOUNT_DISABLED");
    }

    public static AccessDeniedException permissionRevoked() {
        return new AccessDeniedException(
                "Your permission to access this resource has been revoked",
                "PERMISSION_REVOKED");
    }
}
