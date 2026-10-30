package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated list response for Sections with live metric cards:
 * - totalSections
 * - activeSections
 * - totalStudents
 * - averageStudents
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionListResponse {

    private List<SectionResponse> sections;
    private List<SectionResponse> content;

    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    // ── Metric Cards ───────────────────────────────────────────────────────
    private long totalSections;
    private long activeSections;
    private long totalStudents;
    private Double averageStudents;
}

