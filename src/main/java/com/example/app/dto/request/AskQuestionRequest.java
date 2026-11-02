package com.example.app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for asking a question to Gemini AI")
public class AskQuestionRequest {

    @NotBlank(message = "Question cannot be blank")
    @Schema(description = "The question typed by the content admin", example = "What is photosynthesis?")
    private String question;

    @Schema(description = "Optional filter or hint by target class (e.g., Class 10)", example = "Class 10")
    private String targetClass;

    @Schema(description = "Optional filter or hint by subject (e.g., Science, Biology)", example = "Science")

    private String subject;

    @Schema(description = "Optional filter or hint by topic/chapter", example = "Photosynthesis")
    private String topic;

    @Schema(description = "Optional content ID if asking specifically about a single content module", example = "1")
    private Long contentId;

    @Schema(description = "Optional conversation history messages for context")
    private List<ChatMessage> history;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        @Schema(description = "Role: user or assistant", example = "user")
        private String role;

        @Schema(description = "Message content", example = "What is photosynthesis?")
        private String content;
    }
}

