package com.example.app.service;

import com.example.app.dto.request.AskQuestionRequest;
import com.example.app.dto.response.AskQuestionResponse;

public interface AiKnowledgeService {

    /**
     * Answers questions asked by Content Admin using Gemini Free Model.
     * Optionally augments context using relevant educational content in the library.
     *
     * @param request question request with question text and optional context
     * @return answer with confidence score and sources
     */
    AskQuestionResponse answerQuestion(AskQuestionRequest request);
}

