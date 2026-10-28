package com.example.app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentStatsResponse {

    @JsonProperty("totalContents")
    private long totalContents;

    @JsonProperty("totalContent")
    public long getTotalContent() {
        return totalContents;
    }

    @JsonProperty("textbooks")
    private long textbooks;

    @JsonProperty("syllabus")
    private long syllabus;

    @JsonProperty("notesAndReferences")
    private long notesAndReferences;

    @JsonProperty("notes")
    public long getNotes() {
        return notesAndReferences;
    }
}
