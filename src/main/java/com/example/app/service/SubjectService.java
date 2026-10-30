package com.example.app.service;

import com.example.app.dto.request.SubjectCreateRequest;
import com.example.app.dto.request.SubjectUpdateRequest;
import com.example.app.dto.response.SubjectListResponse;
import com.example.app.dto.response.SubjectResponse;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Pageable;

public interface SubjectService {

    /**
     * Create a new subject.
     */
    SubjectResponse createSubject(SubjectCreateRequest request);

    /**
     * Update an existing subject.
     */
    SubjectResponse updateSubject(Long id, SubjectUpdateRequest request);

    /**
     * Get subject by ID.
     */
    SubjectResponse getSubject(Long id);

    /**
     * Soft delete subject by ID (sets status to INACTIVE).
     */
    void deleteSubject(Long id);

    /**
     * View subjects in table with filters and count.
     */
    SubjectListResponse listSubjects(Long schoolId, String search, String className, UserStatus status, Pageable pageable);

    /**
     * Export all matching subjects to Excel (.xlsx).
     */
    byte[] exportSubjectsToExcel(Long schoolId, String search, String className, UserStatus status);
}

