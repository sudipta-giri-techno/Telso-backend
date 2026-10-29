package com.example.app.service;

import com.example.app.dto.request.TeacherCreateRequest;
import com.example.app.dto.request.TeacherUpdateRequest;
import com.example.app.dto.response.TeacherListResponse;
import com.example.app.dto.response.TeacherResponse;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface TeacherService {

    /** Create a new teacher (school admin only). */
    TeacherResponse createTeacher(TeacherCreateRequest request);

    /** Update an existing teacher's details and/or class assignments. */
    TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request);

    /** View a single teacher by ID. */
    TeacherResponse getTeacher(Long id);

    /**
     * List teachers with optional filters.
     * Returns paginated list plus total count.
     */
    TeacherListResponse listTeachers(
        Long schoolId,
        String search,
        String subject,
        String classSection,
        UserStatus status,
        Pageable pageable
    );

    /**
     * Export all matching teachers as an Excel (.xlsx) file.
     * Returns the raw bytes of the workbook.
     */
    byte[] exportTeachersToExcel(
        Long schoolId,
        String search,
        String subject,
        String classSection,
        UserStatus status
    );

    /**
     * Bulk-upload teachers from an Excel file.
     * Returns a summary response (created count, skipped duplicates, etc.).
     */
    TeacherListResponse bulkUploadTeachers(Long schoolId, MultipartFile file);
}
