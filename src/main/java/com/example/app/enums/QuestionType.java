package com.example.app.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Question types supported in Telso Saarthi Question Sets.
 */
public enum QuestionType {
    MCQ("MCQ (Multiple Choice)"),
    TRUE_FALSE("True / False"),
    SHORT_ANSWER("Short Answer"),
    LONG_ANSWER("Long Answer"),
    MATCH_THE_FOLLOWING("Match the Following"),
    FILL_IN_THE_BLANKS("Fill in the Blanks");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static QuestionType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim().toUpperCase().replace(" ", "_").replace("/", "_").replace("-", "_");
        for (QuestionType type : values()) {
            if (type.name().equalsIgnoreCase(clean)) {
                return type;
            }
        }
        if (clean.contains("MCQ") || clean.contains("MULTIPLE_CHOICE")) {
            return MCQ;
        }
        if (clean.contains("TRUE") || clean.contains("FALSE")) {
            return TRUE_FALSE;
        }
        if (clean.contains("SHORT")) {
            return SHORT_ANSWER;
        }
        if (clean.contains("LONG")) {
            return LONG_ANSWER;
        }
        if (clean.contains("MATCH")) {
            return MATCH_THE_FOLLOWING;
        }
        if (clean.contains("FILL") || clean.contains("BLANK")) {
            return FILL_IN_THE_BLANKS;
        }
        throw new IllegalArgumentException("Unknown question type: " + value);
    }
}

