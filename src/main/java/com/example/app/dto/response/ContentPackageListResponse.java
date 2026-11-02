package com.example.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContentPackageListResponse {
    private ContentPackageStatsResponse stats;
    private List<ContentPackageResponse> packages;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
