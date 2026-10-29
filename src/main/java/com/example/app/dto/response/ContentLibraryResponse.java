package com.example.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Combined response for the content library endpoint (GET /api/contents).
 * Always includes status counts plus the (filtered/paginated) content list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentLibraryResponse {

    /**
     * Aggregated status counts (always present regardless of active filters).
     */
    private ContentStatsResponse stats;

    /**
     * Paginated, optionally-filtered list of content items.
     */
    private PagedResponse<ContentResponse> contents;
}

