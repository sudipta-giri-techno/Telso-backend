package com.example.app.controller;

import com.example.app.dto.request.ContentUploadRequest;
import com.example.app.dto.response.ContentLibraryResponse;
import com.example.app.dto.response.ContentResponse;
import com.example.app.enums.ContentType;
import com.example.app.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Validated
@Tag(name = "Content Management", description = "Endpoints for educational content upload, library viewing, and metrics")
@SecurityRequirement(name = "bearerAuth")
public class ContentController {

    private final ContentService contentService;

    /**
     * Upload educational content.
     * Accessible exclusively to CONTENT_ADMIN.
     * Contains language PDF file paths for English and Manipuri.
     */
    @PostMapping
    @PreAuthorize("hasRole('CONTENT_ADMIN')")
    @Operation(summary = "Upload educational content",
               description = """
                   Only users with CONTENT_ADMIN role can upload content.
                   Provide 'englishFilePath' and/or 'manipuriFilePath' with the stored PDF paths.
                   Language selection is also auto-inferred from the provided file paths.
                   """)
    public ResponseEntity<ContentResponse> uploadContent(
            @Valid @RequestBody ContentUploadRequest request,
            Principal principal) {
        String uploaderEmail = principal != null ? principal.getName() : null;
        ContentResponse response = contentService.uploadContent(request, uploaderEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Show / search content library with filters and pagination.
     * Always includes aggregated status counts (stats) regardless of active filters.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Show content library (paginated with filters + status counts)",
               description = """
                   Search and filter educational content by title, subject, chapter, topic, class, and language.
                   The response always contains 'stats' (total, textbooks, syllabus, notes counts over all active
                   content) in addition to the filtered/paginated 'contents' list.
                   """)
    public ResponseEntity<ContentLibraryResponse> getContents(
            @Parameter(description = "Search term in title, subject, chapter, topic, or keywords")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by content type: TEXTBOOK, SYLLABUS, NOTES_AND_REFERENCES")
            @RequestParam(required = false) ContentType contentType,

            @Parameter(description = "Filter by class grade (e.g. 8, 6 - 10)")
            @RequestParam(name = "class", required = false) String targetClass,

            @Parameter(description = "Filter by subject (e.g. Science, Mathematics)")
            @RequestParam(required = false) String subject,

            @Parameter(description = "Filter by language (EN or MR)")
            @RequestParam(required = false) String language,

            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page index must not be negative") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size must not exceed 100") int size
    ) {
        return ResponseEntity.ok(contentService.getContents(
                search, contentType, targetClass, subject, language, page, size));
    }

    /**
     * Show a single content item by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Show single content item by ID")
    public ResponseEntity<ContentResponse> getContentById(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getContentById(id));
    }

    /**
     * Soft-delete content item by ID.
     * Sets active = false; the row is NOT physically removed from the database.
     * Accessible exclusively to CONTENT_ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CONTENT_ADMIN')")
    @Operation(summary = "Soft-delete content item (CONTENT_ADMIN only)",
               description = "Marks the content as inactive (active = false). The record is retained in the database.")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}
