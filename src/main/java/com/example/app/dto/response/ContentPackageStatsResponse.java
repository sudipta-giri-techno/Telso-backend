package com.example.app.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentPackageStatsResponse {
    private long totalPackages;
    private long installed;
    private long downloading;
    private long notInstalled;
}
