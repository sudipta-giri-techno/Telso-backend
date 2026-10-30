package com.example.app.service;

import com.example.app.dto.request.ClassroomOperatorCreateRequest;
import com.example.app.dto.request.ClassroomOperatorUpdateRequest;
import com.example.app.dto.response.ClassroomOperatorBulkUploadResponse;
import com.example.app.dto.response.ClassroomOperatorListResponse;
import com.example.app.dto.response.ClassroomOperatorResponse;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ClassroomOperatorService {

    /**
     * 1. Create a new classroom operator.
     */
    ClassroomOperatorResponse createClassroomOperator(ClassroomOperatorCreateRequest request);

    /**
     * Update an existing classroom operator.
     */
    ClassroomOperatorResponse updateClassroomOperator(Long id, ClassroomOperatorUpdateRequest request);

    /**
     * View single classroom operator by ID.
     */
    ClassroomOperatorResponse getClassroomOperator(Long id);

    /**
     * Soft delete classroom operator by ID (updates status to INACTIVE without removing record).
     */
    void deleteClassroomOperator(Long id);

    /**
     * 4. View classroom operators in table with filters.
     * Always returns top KPI statistics:
     * - total classroom operators
     * - active operators
     * - recently added (last 30 days)
     * - assigned classrooms
     * - inactive operators
     */
    ClassroomOperatorListResponse listClassroomOperators(
        Long schoolId,
        String search,
        UserStatus status,
        String assignedClassroom,
        Pageable pageable
    );

    /**
     * 3. Download / export of all matching classroom operators in Excel (.xlsx) format.
     */
    byte[] exportClassroomOperatorsToExcel(
        Long schoolId,
        String search,
        UserStatus status,
        String assignedClassroom
    );

    /**
     * Download Excel template for bulk upload.
     */
    byte[] downloadTemplate();

    /**
     * 2. Bulk upload classroom operators from an Excel (.xlsx / .xls) file.
     */
    ClassroomOperatorBulkUploadResponse bulkUploadClassroomOperators(Long schoolId, MultipartFile file);
}

