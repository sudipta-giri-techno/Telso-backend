package com.example.app.dto.response;

import com.example.app.entity.QuestionSet;
import com.example.app.enums.BloomsTaxonomy;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionSetStatus;
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
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSetResponse {

    private Long id;
    private String setId;
    private String name;

    @JsonProperty("title")
    public String getTitle() {
        return name;
    }

    @JsonProperty("questionSetName")
    public String getQuestionSetName() {
        return name;
    }

    @JsonProperty("class")
    private String targetClass;

    private String subject;
    private String chapter;
    private String topic;
    private String chapterTopic;

    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    private Integer numberOfQuestions;
    private int totalQuestions;
    private int approvedQuestions;

    @Builder.Default
    private Set<QuestionType> questionTypes = new LinkedHashSet<>();
    private List<String> questionTypesDisplay;

    @Builder.Default
    private Set<DifficultyLevel> difficultyLevels = new LinkedHashSet<>();
    private List<String> difficultyLevelsDisplay;

    @Builder.Default
    private Set<BloomsTaxonomy> bloomsTaxonomy = new LinkedHashSet<>();
    private List<String> bloomsTaxonomyDisplay;

    private Boolean includeExplanations;
    private Boolean includeSourceReference;
    private Boolean randomizeOptions;

    private QuestionSetStatus status;
    private String statusDisplay;

    @Builder.Default
    private Set<String> tags = new LinkedHashSet<>();

    private String generatedBy;

    private Long createdById;
    private String createdByName;
    private String createdByEmail;

    @Builder.Default
    private List<QuestionResponse> questions = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuestionSetResponse from(QuestionSet set) {
        return from(set, true);
    }

    public static QuestionSetResponse from(QuestionSet set, boolean includeQuestions) {
        if (set == null) return null;

        String chapterTopic;
        if (set.getChapter() != null && !set.getChapter().isBlank()
                && set.getTopic() != null && !set.getTopic().isBlank()) {
            chapterTopic = set.getChapter() + " / " + set.getTopic();
        } else if (set.getChapter() != null && !set.getChapter().isBlank()) {
            chapterTopic = set.getChapter();
        } else if (set.getTopic() != null && !set.getTopic().isBlank()) {
            chapterTopic = set.getTopic();
        } else {
            chapterTopic = "-";
        }

        Long creatorId = null;
        String creatorName = null;
        String creatorEmail = null;
        if (set.getCreatedBy() != null) {
            creatorId = set.getCreatedBy().getId();
            creatorName = set.getCreatedBy().getFullName();
            creatorEmail = set.getCreatedBy().getEmail();
        }

        List<String> qTypesDisplay = null;
        if (set.getQuestionTypes() != null) {
            qTypesDisplay = set.getQuestionTypes().stream()
                    .map(QuestionSetResponse::formatQuestionType)
                    .collect(Collectors.toList());
        }

        List<String> diffDisplay = null;
        if (set.getDifficultyLevels() != null) {
            diffDisplay = set.getDifficultyLevels().stream()
                    .map(QuestionSetResponse::formatDifficulty)
                    .collect(Collectors.toList());
        }

        List<String> bloomsDisplay = null;
        if (set.getBloomsTaxonomy() != null) {
            bloomsDisplay = set.getBloomsTaxonomy().stream()
                    .map(QuestionSetResponse::formatBloom)
                    .collect(Collectors.toList());
        }

        int totalQuestions = set.getTotalQuestionsCount();
        int approvedQuestions = set.getApprovedQuestionsCount();

        List<QuestionResponse> questionResponses = new ArrayList<>();
        if (includeQuestions && set.getQuestions() != null) {
            questionResponses = set.getQuestions().stream()
                    .map(QuestionResponse::from)
                    .collect(Collectors.toList());
        }

        return QuestionSetResponse.builder()
                .id(set.getId())
                .setId(set.getSetId())
                .name(set.getName())
                .targetClass(set.getTargetClass())
                .subject(set.getSubject())
                .chapter(set.getChapter())
                .topic(set.getTopic())
                .chapterTopic(chapterTopic)
                .languages(set.getLanguages() != null ? new LinkedHashSet<>(set.getLanguages()) : new LinkedHashSet<>())
                .numberOfQuestions(set.getNumberOfQuestions())
                .totalQuestions(totalQuestions)
                .approvedQuestions(approvedQuestions)
                .questionTypes(set.getQuestionTypes() != null ? new LinkedHashSet<>(set.getQuestionTypes()) : new LinkedHashSet<>())
                .questionTypesDisplay(qTypesDisplay)
                .difficultyLevels(set.getDifficultyLevels() != null ? new LinkedHashSet<>(set.getDifficultyLevels()) : new LinkedHashSet<>())
                .difficultyLevelsDisplay(diffDisplay)
                .bloomsTaxonomy(set.getBloomsTaxonomy() != null ? new LinkedHashSet<>(set.getBloomsTaxonomy()) : new LinkedHashSet<>())
                .bloomsTaxonomyDisplay(bloomsDisplay)
                .includeExplanations(set.getIncludeExplanations())
                .includeSourceReference(set.getIncludeSourceReference())
                .randomizeOptions(set.getRandomizeOptions())
                .status(set.getStatus())
                .statusDisplay(formatStatus(set.getStatus()))
                .tags(set.getTags() != null ? new LinkedHashSet<>(set.getTags()) : new LinkedHashSet<>())
                .generatedBy(set.getGeneratedBy())
                .createdById(creatorId)
                .createdByName(creatorName)
                .createdByEmail(creatorEmail)
                .questions(questionResponses)
                .createdAt(set.getCreatedAt())
                .updatedAt(set.getUpdatedAt())
                .build();
    }

    private static String formatStatus(QuestionSetStatus status) {
        if (status == null) return null;
        return switch (status) {
            case DRAFT -> "Draft";
            case READY -> "Ready";
            case ARCHIVED -> "Archived";
        };
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

    private static String formatBloom(BloomsTaxonomy bloom) {
        if (bloom == null) return null;
        return switch (bloom) {
            case REMEMBER -> "Remember";
            case UNDERSTAND -> "Understand";
            case APPLY -> "Apply";
            case ANALYZE -> "Analyze";
            case EVALUATE -> "Evaluate";
            case CREATE -> "Create";
        };
    }
}

