package com.example.app.dto.response;

import com.example.app.entity.Question;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private Long id;
    private Long questionSetId;
    private Integer orderIndex;

    @JsonProperty("question")
    private String questionText;

    @JsonProperty("questionText")
    public String getQuestionTextAlias() {
        return questionText;
    }

    private QuestionType type;
    private String typeDisplay;

    private DifficultyLevel difficulty;
    private String difficultyDisplay;

    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    @JsonProperty("source")
    private String sourceReference;

    @JsonProperty("sourceReference")
    public String getSourceReferenceAlias() {
        return sourceReference;
    }

    private String explanation;

    @Builder.Default
    private List<String> options = new ArrayList<>();

    private String correctAnswer;
    private Boolean isApproved;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuestionResponse from(Question question) {
        if (question == null) return null;

        Long qSetId = question.getQuestionSet() != null ? question.getQuestionSet().getId() : null;
        String typeDisp = question.getQuestionType() != null ? formatQuestionType(question.getQuestionType()) : null;
        String diffDisp = question.getDifficultyLevel() != null ? formatDifficulty(question.getDifficultyLevel()) : null;

        return QuestionResponse.builder()
                .id(question.getId())
                .questionSetId(qSetId)
                .orderIndex(question.getOrderIndex())
                .questionText(question.getQuestionText())
                .type(question.getQuestionType())
                .typeDisplay(typeDisp)
                .difficulty(question.getDifficultyLevel())
                .difficultyDisplay(diffDisp)
                .languages(question.getLanguages() != null ? new LinkedHashSet<>(question.getLanguages()) : new LinkedHashSet<>())
                .sourceReference(question.getSourceReference())
                .explanation(question.getExplanation())
                .options(question.getOptions() != null ? new ArrayList<>(question.getOptions()) : new ArrayList<>())
                .correctAnswer(question.getCorrectAnswer())
                .isApproved(question.getIsApproved())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    private static String formatQuestionType(QuestionType type) {
        if (type == null) return null;
        return switch (type) {
            case MCQ -> "MCQ (Multiple Choice)";
            case TRUE_FALSE -> "True / False";
            case SHORT_ANSWER -> "Short Answer";
            case LONG_ANSWER -> "Long Answer";
            case MATCH_THE_FOLLOWING -> "Match the Following";
            case FILL_IN_THE_BLANKS -> "Fill in the Blanks";
        };
    }

    private static String formatDifficulty(DifficultyLevel difficulty) {
        if (difficulty == null) return null;
        return switch (difficulty) {
            case EASY -> "Easy";
            case MEDIUM -> "Medium";
            case HARD -> "Hard";
        };
    }
}

