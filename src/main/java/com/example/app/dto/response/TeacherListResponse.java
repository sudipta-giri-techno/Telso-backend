package com.example.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Paginated teacher list with total count.
 */
@Data
@Builder
public class TeacherListResponse {
    private long totalTeachers;
    private long totalPages;
    private long currentPage;
    private long pageSize;
    private List<TeacherResponse> teachers;
}
