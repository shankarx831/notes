package com.studentnotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a note (creates new version).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNoteRequest {

    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    private String content;

    private String department;
    private String year;
    private String section;
    private String subject;

    /**
     * Required: Change summary for this version.
     */
    @NotBlank(message = "Change summary is required for updates")
    @Size(max = 500, message = "Change summary must be at most 500 characters")
    private String changeSummary;
}
