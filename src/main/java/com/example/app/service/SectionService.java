package com.example.app.service;

import com.example.app.dto.request.SectionCreateRequest;
import com.example.app.dto.request.SectionUpdateRequest;
import com.example.app.dto.response.SectionListResponse;
import com.example.app.dto.response.SectionResponse;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Pageable;

public interface SectionService {

    /**
     * Create a new section.
     */
    SectionResponse createSection(SectionCreateRequest request);

    /**
     * Update an existing section.
     */
    SectionResponse updateSection(Long id, SectionUpdateRequest request);

    /**
     * Get section by ID.
     */
    SectionResponse getSection(Long id);

    /**
     * Soft delete section by ID (sets status to INACTIVE).
     */
    void deleteSection(Long id);

    /**
     * View sections in table with filters and live metrics.
     */
    SectionListResponse listSections(Long schoolId, String search, String className, UserStatus status, Pageable pageable);

    /**
     * Export all matching sections to Excel (.xlsx).
     */
    byte[] exportSectionsToExcel(Long schoolId, String search, String className, UserStatus status);
}

