package com.example.app.controller;

import com.example.app.dto.request.AskQuestionRequest;
import com.example.app.dto.response.AskQuestionResponse;
import com.example.app.service.GeminiAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Knowledge Testing", description = "Endpoints for Content Admin AI Knowledge Testing with Google Gemini")
@SecurityRequirement(name = "bearerAuth")
public class AiKnowledgeController {

    private final GeminiAiService geminiAiService;

    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('CONTENT_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Ask question to Gemini AI (Free Model)",
               description = "Answers educational and content questions asked by Content Admin or Central Admin using Google Gemini API.")
    public ResponseEntity<AskQuestionResponse> askQuestion(@Valid @RequestBody AskQuestionRequest request) {
        AskQuestionResponse response = geminiAiService.answerQuestion(request);
        return ResponseEntity.ok(response);
    }
}

