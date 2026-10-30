package com.example.app.service;

import com.example.app.dto.request.StudentCreateRequest;
import com.example.app.dto.request.StudentUpdateRequest;
import com.example.app.dto.response.StudentBulkUploadResponse;
import com.example.app.dto.response.StudentListResponse;
import com.example.app.dto.response.StudentResponse;
import com.example.app.enums.Gender;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {

    /**
     * Create a new student record.
     */
    StudentResponse createStudent(StudentCreateRequest request);

    /**
     * Update an existing student record.
     */
    StudentResponse updateStudent(Long id, StudentUpdateRequest request);

    /**
     * Get a single student by ID.
     */
    StudentResponse getStudent(Long id);

    /**
     * Soft delete student by ID (sets status to INACTIVE without removing the record).
     */
    void deleteStudent(Long id);

    /**
     * View students in table with filters (search, class, section, status, gender) and counts.
     */
    StudentListResponse listStudents(
        Long schoolId,
        String search,
        String className,
        String section,
        UserStatus status,
        Gender gender,
        Pageable pageable
    );

    /**
     * Download / export all matching students in Excel (.xlsx) format.
     */
    byte[] exportStudentsToExcel(
        Long schoolId,
        String search,
        String className,
        String section,
        UserStatus status,
        Gender gender
    );

    /**
     * Bulk upload students from an Excel (.xlsx / .xls) file.
     */
    StudentBulkUploadResponse bulkUploadStudents(Long schoolId, MultipartFile file);
}

