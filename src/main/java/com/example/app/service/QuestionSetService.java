package com.example.app.service;

import com.example.app.dto.request.QuestionCreateRequest;
import com.example.app.dto.request.QuestionSetCreateRequest;
import com.example.app.dto.request.QuestionSetUpdateRequest;
import com.example.app.dto.request.QuestionUpdateRequest;
import com.example.app.dto.response.PagedResponse;
import com.example.app.dto.response.QuestionResponse;
import com.example.app.dto.response.QuestionSetListResponse;
import com.example.app.dto.response.QuestionSetResponse;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionSetStatus;
import com.example.app.enums.QuestionType;

import java.util.List;

public interface QuestionSetService {

    /**
     * Create a new Question Set, optionally with initial questions.
     */
    QuestionSetResponse createQuestionSet(QuestionSetCreateRequest request, String creatorEmail);

    /**
     * Get paginated Question Sets with optional filtering.
     * Always includes live status counts (total, ready, draft, archived)
     * so callers do not need a separate stats endpoint.
     */
    QuestionSetListResponse getQuestionSets(
            String search,
            String targetClass,
            String subject,
            String chapter,
            String topic,
            QuestionSetStatus status,
            String language,
            int page,
            int size
    );

    /**
     * Get Question Set by ID (with all attached questions).
     */
    QuestionSetResponse getQuestionSetById(Long id);

    /**
     * Update Question Set details, including adding or modifying questions.
     */
    QuestionSetResponse updateQuestionSet(Long id, QuestionSetUpdateRequest request);

    /**
     * Delete a Question Set and all its questions.
     */
    void deleteQuestionSet(Long id);

    /**
     * Add a single question to an existing Question Set.
     */
    QuestionResponse addQuestionToSet(Long questionSetId, QuestionCreateRequest request);

    /**
     * Add multiple questions to an existing Question Set.
     */
    List<QuestionResponse> addQuestionsToSet(Long questionSetId, List<QuestionCreateRequest> requests);

    /**
     * Update an individual question within a Question Set.
     */
    QuestionResponse updateQuestion(Long questionSetId, Long questionId, QuestionUpdateRequest request);

    /**
     * Delete an individual question from a Question Set.
     */
    void deleteQuestion(Long questionSetId, Long questionId);

    /**
     * Get paginated/filtered questions belonging to a specific Question Set.
     */
    PagedResponse<QuestionResponse> getQuestionsBySetId(
            Long questionSetId,
            String search,
            QuestionType type,
            DifficultyLevel difficulty,
            int page,
            int size
    );
}

