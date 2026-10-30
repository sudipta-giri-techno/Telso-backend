package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated table view response for Students.
 * Includes student records and overall counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentListResponse {

    // ── Table Content & Pagination ─────────────────────────────────────────
    private List<StudentResponse> students;

    /** Alias for students list */
    private List<StudentResponse> content;

    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    // ── Metrics ────────────────────────────────────────────────────────────
    private long totalStudents;
    private long activeStudents;
    private long inactiveStudents;
    private long maleStudents;
    private long femaleStudents;
}

