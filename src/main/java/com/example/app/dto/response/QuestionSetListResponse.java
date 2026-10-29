package com.example.app.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Merged response for the "Get Question Sets" endpoint.
 * Combines the paginated list with live status/stats counts so callers
 * never need to hit a separate /status or /stats endpoint.
 */
@Data
@Builder
public class QuestionSetListResponse {

    // ── Pagination ────────────────────────────────────────────────────
    private java.util.List<QuestionSetResponse> content;
    private int  page;
    private int  size;
    private long totalItems;
    private int  totalPages;

    // ── Status counts (always returned, regardless of active filters) ─
    private long totalQuestionSets;
    private long readySets;
    private long draftSets;
    private long archivedSets;
}

