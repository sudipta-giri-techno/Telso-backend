package com.example.app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated status metrics for Question Sets.
 * Matches the metric cards displayed on the Question Sets view page:
 * - Total Question Sets
 * - Ready Sets
 * - Draft Sets
 * - Archived Sets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSetStatsResponse {

    @JsonProperty("totalQuestionSets")
    private long totalQuestionSets;

    @JsonProperty("totalSets")
    public long getTotalSets() {
        return totalQuestionSets;
    }

    @JsonProperty("readySets")
    private long readySets;

    @JsonProperty("ready")
    public long getReady() {
        return readySets;
    }

    @JsonProperty("draftSets")
    private long draftSets;

    @JsonProperty("draft")
    public long getDraft() {
        return draftSets;
    }

    @JsonProperty("archivedSets")
    private long archivedSets;

    @JsonProperty("archiveSets")
    public long getArchiveSets() {
        return archivedSets;
    }

    @JsonProperty("archived")
    public long getArchived() {
        return archivedSets;
    }
}

