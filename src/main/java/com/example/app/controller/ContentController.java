package com.example.app.controller;

import com.example.app.dto.request.ContentUploadRequest;
import com.example.app.dto.response.ContentResponse;
import com.example.app.dto.response.ContentStatsResponse;
import com.example.app.dto.response.PagedResponse;
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
     * Contains language options (English, Manipuri, or both).
     */
    @PostMapping
    @PreAuthorize("hasRole('CONTENT_ADMIN')")
    @Operation(summary = "Upload educational content",
               description = "Only users with CONTENT_ADMIN role can upload content with selected language options (English, Manipuri, or both).")
    public ResponseEntity<ContentResponse> uploadContent(
            @Valid @RequestBody ContentUploadRequest request,
            Principal principal) {
        String uploaderEmail = principal != null ? principal.getName() : null;
        ContentResponse response = contentService.uploadContent(request, uploaderEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve status metrics of contents (total contents, textbooks, syllabus, notes and references).
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get content statistics / status",
               description = "Returns aggregated counts for Total Content, Textbooks, Syllabus, and Notes & References.")
    public ResponseEntity<ContentStatsResponse> getContentStatus() {
        return ResponseEntity.ok(contentService.getContentStats());
    }

    /**
     * Show / search content library with filters and pagination.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Show content library (paginated with filters)",
               description = "Search and filter educational content by title, subject, chapter, topic, class, and language.")
    public ResponseEntity<PagedResponse<ContentResponse>> getContents(
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
     * Delete content item by ID.
     * Accessible exclusively to CONTENT_ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CONTENT_ADMIN')")
    @Operation(summary = "Delete content item (CONTENT_ADMIN only)")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}
