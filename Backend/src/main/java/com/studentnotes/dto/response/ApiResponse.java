package com.studentnotes.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API response wrapper.
 * All API responses follow this structure for consistency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Whether the request was successful.
     */
    private boolean success;

    /**
     * The response payload (for successful requests).
     */
    private T data;

    /**
     * Error details (for failed requests).
     */
    private ErrorDetails error;

    /**
     * Correlation ID for request tracing.
     */
    private String correlationId;

    /**
     * Timestamp of the response.
     */
    private Instant timestamp;

    /**
     * Pagination metadata (if applicable).
     */
    private PageInfo pagination;

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response with data and pagination.
     */
    public static <T> ApiResponse<T> success(T data, PageInfo pagination) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .pagination(pagination)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response with data and correlation ID.
     */
    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response.
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .message(message)
                        .code(errorCode)
                        .build())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response with correlation ID.
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, String correlationId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .message(message)
                        .code(errorCode)
                        .build())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response with details.
     */
    public static <T> ApiResponse<T> error(ErrorDetails error, String correlationId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String message;
        private String code;
        private java.util.Map<String, String> fieldErrors;
        private String details;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public static PageInfo from(org.springframework.data.domain.Page<?> page) {
            return PageInfo.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .build();
        }
    }
}
