package com.studentnotes.exception;

/**
 * Exception thrown when request validation fails.
 */
public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
    }

    public ValidationException(String message, String errorCode) {
        super(message, errorCode, 400);
    }

    public static ValidationException requiredField(String fieldName) {
        return new ValidationException(
                String.format("Field '%s' is required", fieldName),
                "REQUIRED_FIELD_MISSING");
    }

    public static ValidationException invalidFormat(String fieldName, String expectedFormat) {
        return new ValidationException(
                String.format("Field '%s' must be in format: %s", fieldName, expectedFormat),
                "INVALID_FORMAT");
    }

    public static ValidationException fileTooLarge(long maxSizeBytes) {
        return new ValidationException(
                String.format("File size exceeds maximum allowed (%d bytes)", maxSizeBytes),
                "FILE_TOO_LARGE");
    }

    public static ValidationException unsupportedFileType(String mimeType) {
        return new ValidationException(
                String.format("File type '%s' is not supported", mimeType),
                "UNSUPPORTED_FILE_TYPE");
    }

    public static ValidationException invalidPagination() {
        return new ValidationException(
                "Page number must be >= 0 and page size must be between 1 and 100",
                "INVALID_PAGINATION");
    }
}
