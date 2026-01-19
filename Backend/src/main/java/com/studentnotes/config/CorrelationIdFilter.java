package com.studentnotes.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that assigns/propagates correlation IDs for request tracing.
 * 
 * - If client sends X-Correlation-ID header, it is used
 * - Otherwise, a new UUID is generated
 * - The correlation ID is added to:
 * - MDC (for logging)
 * - Response header (for client-side correlation)
 * 
 * This runs FIRST in the filter chain.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    // Additional context for structured logging
    public static final String REQUEST_PATH_MDC_KEY = "requestPath";
    public static final String REQUEST_METHOD_MDC_KEY = "requestMethod";
    public static final String USER_ID_MDC_KEY = "userId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            // Get or generate correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Generate unique request ID (even if correlation ID is reused)
            String requestId = UUID.randomUUID().toString().substring(0, 8);

            // Add to MDC for structured logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
            MDC.put(REQUEST_PATH_MDC_KEY, request.getRequestURI());
            MDC.put(REQUEST_METHOD_MDC_KEY, request.getMethod());

            // Store in request attributes for later use
            request.setAttribute(CORRELATION_ID_MDC_KEY, correlationId);

            // Add to response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            // Log request completion
            long duration = System.currentTimeMillis() - startTime;
            logger.info(String.format("Completed %s %s - Status: %d - Duration: %dms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration));

            // Clean up MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
            MDC.remove(REQUEST_ID_MDC_KEY);
            MDC.remove(REQUEST_PATH_MDC_KEY);
            MDC.remove(REQUEST_METHOD_MDC_KEY);
            MDC.remove(USER_ID_MDC_KEY);
        }
    }

    /**
     * Gets the current correlation ID from MDC.
     */
    public static String getCurrentCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    /**
     * Sets the user ID in MDC for logging context.
     * Should be called after authentication.
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID_MDC_KEY, userId);
        }
    }
}
