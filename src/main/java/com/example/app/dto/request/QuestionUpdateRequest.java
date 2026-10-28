package com.example.app.dto.request;

import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class QuestionUpdateRequest {

    private Integer orderIndex;

    @JsonAlias({"questionText", "text"})
    @JsonProperty("question")
    private String question;

    @JsonAlias({"questionType"})
    @JsonProperty("type")
    private QuestionType type;

    @JsonAlias({"difficultyLevel"})
    @JsonProperty("difficulty")
    private DifficultyLevel difficulty;

    private Set<String> languages;

    @Size(max = 255, message = "Source reference must not exceed 255 characters")
    @JsonAlias({"sourceReference"})
    @JsonProperty("source")
    private String source;

    private String explanation;

    private List<String> options;

    private String correctAnswer;

    private Boolean isApproved;

    public Set<String> resolveLanguages() {
        if (languages == null) {
            return null;
        }
        Set<String> resolved = new LinkedHashSet<>();
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
        return resolved;
    }
}

