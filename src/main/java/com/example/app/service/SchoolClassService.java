package com.example.app.service;

import com.example.app.dto.request.SchoolClassCreateRequest;
import com.example.app.dto.request.SchoolClassUpdateRequest;
import com.example.app.dto.response.SchoolClassListResponse;
import com.example.app.dto.response.SchoolClassResponse;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Pageable;

public interface SchoolClassService {

    /**
     * Create a new class.
     */
    SchoolClassResponse createClass(SchoolClassCreateRequest request);

    /**
     * Update an existing class.
     */
    SchoolClassResponse updateClass(Long id, SchoolClassUpdateRequest request);

    /**
     * Get class by ID.
     */
    SchoolClassResponse getClass(Long id);

    /**
     * Soft delete class by ID (sets status to INACTIVE).
     */
    void deleteClass(Long id);

    /**
     * View classes in table with filters and live metrics.
     */
    SchoolClassListResponse listClasses(Long schoolId, String search, UserStatus status, Pageable pageable);

    /**
     * Export all matching classes to Excel (.xlsx).
     */
    byte[] exportClassesToExcel(Long schoolId, String search, UserStatus status);
}

