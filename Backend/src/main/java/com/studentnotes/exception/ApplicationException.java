package com.studentnotes.exception;

/**
 * Base exception for all application-specific exceptions.
 * Includes error code for client-side handling.
 */
public class ApplicationException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public ApplicationException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ApplicationException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
