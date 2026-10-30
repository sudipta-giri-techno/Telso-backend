package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated table view response for Classroom Operators.
 * Always includes the top-level status metrics (KPI cards) regardless of filters applied:
 * - total operators
 * - active operators
 * - recently added (last 30 days)
 * - assigned classrooms count
 * - inactive operators
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomOperatorListResponse {

    // ── Table Content & Pagination ─────────────────────────────────────────
    private List<ClassroomOperatorResponse> operators;

    /** Alias for operators list */
    private List<ClassroomOperatorResponse> content;

    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    // ── Live KPI Metrics (always returned as requested in requirement 4) ───
    private long totalOperators;
    private long activeOperators;
    private long inactiveOperators;
    private long recentlyAdded;
    private long assignedClassrooms;
    private Double activePercentage;
    private Double inactivePercentage;
}

