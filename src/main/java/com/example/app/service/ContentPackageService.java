package com.example.app.service;

import com.example.app.dto.response.ContentPackageListResponse;
import com.example.app.dto.response.ContentPackageResponse;
import com.example.app.dto.response.ContentPackageStatsResponse;
import com.example.app.enums.ContentInstallationStatus;
import org.springframework.data.domain.Pageable;

public interface ContentPackageService {

    ContentPackageListResponse listPackages(
        Long operatorId,
        String search,
        String className,
        String subject,
        String language,
        ContentInstallationStatus status,
        Pageable pageable
    );

    ContentPackageResponse getPackage(Long operatorId, Long contentId);

    ContentPackageResponse installPackage(Long operatorId, Long contentId);

    ContentPackageStatsResponse getStats(Long operatorId);

    byte[] exportPackagesToExcel(Long operatorId) throws Exception;
}
