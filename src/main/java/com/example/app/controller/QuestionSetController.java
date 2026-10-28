package com.example.app.controller;

import com.example.app.dto.request.QuestionCreateRequest;
import com.example.app.dto.request.QuestionSetCreateRequest;
import com.example.app.dto.request.QuestionSetUpdateRequest;
import com.example.app.dto.request.QuestionUpdateRequest;
import com.example.app.dto.response.PagedResponse;
import com.example.app.dto.response.QuestionResponse;
import com.example.app.dto.response.QuestionSetResponse;
import com.example.app.dto.response.QuestionSetStatsResponse;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionSetStatus;
import com.example.app.enums.QuestionType;
import com.example.app.service.QuestionSetService;
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
import java.util.List;

@RestController
@RequestMapping("/api/question-sets")
@RequiredArgsConstructor
@Validated
@Tag(name = "Question Set Management", description = "Endpoints for creating, managing, and viewing Question Sets and Questions")
@SecurityRequirement(name = "bearerAuth")
public class QuestionSetController {

    private final QuestionSetService questionSetService;

    /**
     * Create a new Question Set. Content admin can optionally supply initial questions.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Create Question Set",
               description = "Content admin can create a Question Set and optionally include initial questions.")
    public ResponseEntity<QuestionSetResponse> createQuestionSet(
            @Valid @RequestBody QuestionSetCreateRequest request,
            Principal principal) {
        String creatorEmail = principal != null ? principal.getName() : null;
        QuestionSetResponse response = questionSetService.createQuestionSet(request, creatorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve status metrics of question sets (total, ready, draft, archived sets).
     */
    @GetMapping({"/status", "/stats"})
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get Question Set Status Metrics",
               description = "Returns aggregated counts for Total Question Sets, Ready Sets, Draft Sets, and Archived Sets.")
    public ResponseEntity<QuestionSetStatsResponse> getQuestionSetStatus() {
        return ResponseEntity.ok(questionSetService.getQuestionSetStats());
    }

    /**
     * Retrieve paginated Question Sets with search and filters.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get Question Sets (paginated with filters)",
               description = "Search and filter question sets by name, class, subject, chapter, topic, status, and language.")
    public ResponseEntity<PagedResponse<QuestionSetResponse>> getQuestionSets(
            @Parameter(description = "Search term in name, setId, class, subject, chapter, or topic")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by class (e.g. 8)")
            @RequestParam(name = "class", required = false) String targetClass,

            @Parameter(description = "Filter by subject (e.g. Science)")
            @RequestParam(required = false) String subject,

            @Parameter(description = "Filter by chapter (e.g. Human Body)")
            @RequestParam(required = false) String chapter,

            @Parameter(description = "Filter by topic (e.g. Digestive System)")
            @RequestParam(required = false) String topic,

            @Parameter(description = "Filter by status: READY, DRAFT, ARCHIVED")
            @RequestParam(required = false) QuestionSetStatus status,

            @Parameter(description = "Filter by language (e.g. EN, MR)")
            @RequestParam(required = false) String language,

            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page index must not be negative") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size must not exceed 100") int size
    ) {
        return ResponseEntity.ok(questionSetService.getQuestionSets(
                search, targetClass, subject, chapter, topic, status, language, page, size));
    }

    /**
     * Get a single Question Set by ID (includes its questions list).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get Question Set by ID",
               description = "Returns question set details and full list of contained questions.")
    public ResponseEntity<QuestionSetResponse> getQuestionSetById(@PathVariable Long id) {
        return ResponseEntity.ok(questionSetService.getQuestionSetById(id));
    }

    /**
     * Update an existing Question Set details and/or questions.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Update Question Set",
               description = "Edit Question Set metadata or replace questions.")
    public ResponseEntity<QuestionSetResponse> updateQuestionSet(
            @PathVariable Long id,
            @Valid @RequestBody QuestionSetUpdateRequest request) {
        return ResponseEntity.ok(questionSetService.updateQuestionSet(id, request));
    }

    /**
     * Delete a Question Set and its questions.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Delete Question Set",
               description = "Deletes a question set and all its associated questions.")
    public ResponseEntity<Void> deleteQuestionSet(@PathVariable Long id) {
        questionSetService.deleteQuestionSet(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a question to an existing Question Set.
     */
    @PostMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Add Question to Question Set",
               description = "Allows Content Admin to further add a question to an existing Question Set.")
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionCreateRequest request) {
        QuestionResponse response = questionSetService.addQuestionToSet(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Add multiple questions to an existing Question Set.
     */
    @PostMapping("/{id}/questions/bulk")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Add Multiple Questions to Question Set",
               description = "Allows Content Admin to add multiple questions in bulk to a Question Set.")
    public ResponseEntity<List<QuestionResponse>> addQuestionsBulk(
            @PathVariable Long id,
            @Valid @RequestBody List<QuestionCreateRequest> requests) {
        List<QuestionResponse> responses = questionSetService.addQuestionsToSet(id, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Edit a specific question in a Question Set.
     */
    @PutMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Update Question in Question Set",
               description = "Edit an existing question under a Question Set.")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionUpdateRequest request) {
        return ResponseEntity.ok(questionSetService.updateQuestion(id, questionId, request));
    }

    /**
     * Delete a question from a Question Set.
     */
    @DeleteMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Delete Question from Question Set",
               description = "Remove a question from a Question Set.")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @PathVariable Long questionId) {
        questionSetService.deleteQuestion(id, questionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get paginated questions of a Question Set with optional search and filters.
     */
    @GetMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get Questions in Question Set",
               description = "Get paginated questions in a Question Set with type and difficulty filters.")
    public ResponseEntity<PagedResponse<QuestionResponse>> getQuestions(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page index must not be negative") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size must not exceed 100") int size
    ) {
        return ResponseEntity.ok(questionSetService.getQuestionsBySetId(id, search, type, difficulty, page, size));
    }
}

