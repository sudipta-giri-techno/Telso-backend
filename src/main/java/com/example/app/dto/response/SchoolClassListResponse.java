package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated list response for Classes along with top-level KPI metrics:
 * - totalClasses
 * - totalSections
 * - totalStudents
 * - averageSections
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassListResponse {

    private List<SchoolClassResponse> classes;
    private List<SchoolClassResponse> content;

    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    // ── Metric Cards ───────────────────────────────────────────────────────
    private long totalClasses;
    private long totalSections;
    private long totalStudents;
    private Double averageSections;
}

