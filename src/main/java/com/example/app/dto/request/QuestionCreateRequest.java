package com.example.app.dto.request;

import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class QuestionCreateRequest {

    private Integer orderIndex;

    @NotBlank(message = "Question text is required")
    @JsonAlias({"questionText", "text"})
    @JsonProperty("question")
    private String question;

    @NotNull(message = "Question type is required")
    @JsonAlias({"questionType"})
    @JsonProperty("type")
    private QuestionType type;

    @JsonAlias({"difficultyLevel"})
    @JsonProperty("difficulty")
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    @Size(max = 255, message = "Source reference must not exceed 255 characters")
    @JsonAlias({"sourceReference"})
    @JsonProperty("source")
    private String source;

    private String explanation;

    @Builder.Default
    private List<String> options = new ArrayList<>();

    private String correctAnswer;

    @Builder.Default
    private Boolean isApproved = true;

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
        return resolved;
    }
}

