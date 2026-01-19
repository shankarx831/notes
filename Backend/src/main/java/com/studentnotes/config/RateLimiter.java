package com.studentnotes.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for write endpoints.
 * 
 * In production, use Redis-based rate limiting for distributed systems.
 * This implementation is for demonstration and single-instance deployments.
 * 
 * Uses sliding window algorithm with per-user limits.
 */
@Component
public class RateLimiter {

    // Configuration
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 30;
    private static final int WRITE_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute

    // Rate limit buckets per user
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Checks if the request should be allowed for a user.
     * 
     * @param userId           The user identifier
     * @param isWriteOperation Whether this is a write operation (stricter limit)
     * @return true if allowed, false if rate limited
     */
    public boolean isAllowed(String userId, boolean isWriteOperation) {
        String key = isWriteOperation ? userId + ":write" : userId + ":read";
        int limit = isWriteOperation ? WRITE_REQUESTS_PER_MINUTE : DEFAULT_REQUESTS_PER_MINUTE;

        RateLimitBucket bucket = buckets.computeIfAbsent(key, k -> new RateLimitBucket());
        return bucket.tryAcquire(limit);
    }

    /**
     * Gets the number of seconds until the rate limit resets.
     */
    public int getRetryAfterSeconds(String userId, boolean isWriteOperation) {
        String key = isWriteOperation ? userId + ":write" : userId + ":read";
        RateLimitBucket bucket = buckets.get(key);
        if (bucket == null) {
            return 0;
        }
        long remaining = WINDOW_SIZE_MS - (System.currentTimeMillis() - bucket.windowStart);
        return (int) Math.ceil(remaining / 1000.0);
    }

    /**
     * Cleans up expired buckets (should be called periodically).
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> now - entry.getValue().windowStart > WINDOW_SIZE_MS * 2);
    }

    /**
     * Rate limit bucket for a single user/operation type.
     */
    private static class RateLimitBucket {
        volatile long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);

        synchronized boolean tryAcquire(int limit) {
            long now = System.currentTimeMillis();

            // Reset window if expired
            if (now - windowStart > WINDOW_SIZE_MS) {
                windowStart = now;
                count.set(0);
            }

            // Check limit
            if (count.get() >= limit) {
                return false;
            }

            count.incrementAndGet();
            return true;
        }
    }
}
