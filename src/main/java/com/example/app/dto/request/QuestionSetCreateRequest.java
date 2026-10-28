package com.example.app.dto.request;

import com.example.app.enums.BloomsTaxonomy;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionSetStatus;
import com.example.app.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSetCreateRequest {

    @NotBlank(message = "Question set name is required")
    @Size(max = 255, message = "Question set name must not exceed 255 characters")
    @JsonAlias({"title", "questionSetName"})
    @JsonProperty("name")
    private String name;

    @Size(max = 50, message = "Set ID must not exceed 50 characters")
    private String setId;

    @NotBlank(message = "Class is required")
    @Size(max = 50, message = "Class must not exceed 50 characters")
    @JsonAlias({"class", "className", "classGrade"})
    @JsonProperty("class")
    private String targetClass;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must not exceed 100 characters")
    private String subject;

    @NotBlank(message = "Chapter is required")
    @Size(max = 150, message = "Chapter must not exceed 150 characters")
    private String chapter;

    @Size(max = 150, message = "Topic must not exceed 150 characters")
    private String topic;

    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    private Boolean english;
    private Boolean manipuri;

    @Min(value = 1, message = "Number of questions must be at least 1")
    private Integer numberOfQuestions;

    @Builder.Default
    private Set<QuestionType> questionTypes = new LinkedHashSet<>();

    @Builder.Default
    private Set<DifficultyLevel> difficultyLevels = new LinkedHashSet<>();

    @Builder.Default
    private Set<BloomsTaxonomy> bloomsTaxonomy = new LinkedHashSet<>();

    @Builder.Default
    private Boolean includeExplanations = false;

    @Builder.Default
    private Boolean includeSourceReference = false;

    @Builder.Default
    private Boolean randomizeOptions = false;

    @Builder.Default
    private QuestionSetStatus status = QuestionSetStatus.DRAFT;

    @Builder.Default
    private Set<String> tags = new LinkedHashSet<>();

    private String generatedBy;

    /**
     * Initial list of questions created along with the question set.
     */
    @Valid
    @Builder.Default
    private List<QuestionCreateRequest> questions = new ArrayList<>();

    public void setClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public void setClassName(String targetClass) {
        this.targetClass = targetClass;
    }

    public void setTags(Object tagInput) {
        if (tagInput == null) {
            this.tags = new LinkedHashSet<>();
            return;
        }
        if (tagInput instanceof Iterable<?> iterable) {
            this.tags = new LinkedHashSet<>();
            for (Object obj : iterable) {
                if (obj != null) {
                    this.tags.add(obj.toString().trim());
                }
            }
        } else if (tagInput instanceof String str) {
            this.tags = new LinkedHashSet<>();
            for (String part : str.split(",")) {
                if (!part.trim().isEmpty()) {
                    this.tags.add(part.trim());
                }
            }
        }
    }

    public Set<String> resolveLanguages() {
        Set<String> resolved = new LinkedHashSet<>();
        if (languages != null) {
            for (String lang : languages) {
                if (lang != null && !lang.isBlank()) {
                    String upper = lang.trim().toUpperCase();
                    if (upper.startsWith("EN")) {
                        resolved.add("EN");
                    } else if (upper.startsWith("MR") || upper.startsWith("MAN") || upper.startsWith("MON")) {
                        resolved.add("MR");
                    } else {
                        resolved.add(upper);
                    }
                }
            }
        }
        if (Boolean.TRUE.equals(english)) {
            resolved.add("EN");
        }
        if (Boolean.TRUE.equals(manipuri)) {
            resolved.add("MR");
        }
        return resolved;
    }
}

