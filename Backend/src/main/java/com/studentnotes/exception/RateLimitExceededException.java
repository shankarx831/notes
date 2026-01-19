package com.studentnotes.exception;

/**
 * Exception thrown when rate limit is exceeded.
 */
public class RateLimitExceededException extends ApplicationException {

    private final int retryAfterSeconds;

    public RateLimitExceededException(int retryAfterSeconds) {
        super(
                String.format("Rate limit exceeded. Please retry after %d seconds", retryAfterSeconds),
                "RATE_LIMIT_EXCEEDED",
                429);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
