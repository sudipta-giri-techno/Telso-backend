package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated list response for Subjects with live metric cards:
 * - totalSubjects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectListResponse {

    private List<SubjectResponse> subjects;
    private List<SubjectResponse> content;

    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    // ── Metric Cards ───────────────────────────────────────────────────────
    private long totalSubjects;
}

