package com.example.app.dto.request;

import com.example.app.enums.BloomsTaxonomy;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionSetStatus;
import com.example.app.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
public class QuestionSetUpdateRequest {

    @Size(max = 255, message = "Question set name must not exceed 255 characters")
    @JsonAlias({"title", "questionSetName"})
    @JsonProperty("name")
    private String name;

    @Size(max = 50, message = "Set ID must not exceed 50 characters")
    private String setId;

    @Size(max = 50, message = "Class must not exceed 50 characters")
    @JsonAlias({"class", "className", "classGrade"})
    @JsonProperty("class")
    private String targetClass;

    @Size(max = 100, message = "Subject must not exceed 100 characters")
    private String subject;

    @Size(max = 150, message = "Chapter must not exceed 150 characters")
    private String chapter;

    @Size(max = 150, message = "Topic must not exceed 150 characters")
    private String topic;

    private Set<String> languages;
    private Boolean english;
    private Boolean manipuri;

    @Min(value = 1, message = "Number of questions must be at least 1")
    private Integer numberOfQuestions;

    private Set<QuestionType> questionTypes;
    private Set<DifficultyLevel> difficultyLevels;
    private Set<BloomsTaxonomy> bloomsTaxonomy;

    private Boolean includeExplanations;
    private Boolean includeSourceReference;
    private Boolean randomizeOptions;

    private QuestionSetStatus status;

    private Set<String> tags;

    private String generatedBy;

    /**
     * Optional updated/replacement list of questions.
     */
    @Valid
    private List<QuestionCreateRequest> questions;

    public void setClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public void setClassName(String targetClass) {
        this.targetClass = targetClass;
    }

    public void setTags(Object tagInput) {
        if (tagInput == null) {
            this.tags = null;
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
        if (languages == null && english == null && manipuri == null) {
            return null;
        }
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

