package com.example.app.controller;

import com.example.app.dto.request.StudentCreateRequest;
import com.example.app.dto.request.StudentUpdateRequest;
import com.example.app.dto.response.StudentBulkUploadResponse;
import com.example.app.dto.response.StudentListResponse;
import com.example.app.dto.response.StudentResponse;
import com.example.app.enums.Gender;
import com.example.app.enums.UserStatus;
import com.example.app.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student management APIs (CRUD, filters, Excel export, and bulk upload)")
public class StudentController {

    private final StudentService studentService;

    // ------------------------------------------------------------------
    // 1. Create Student
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Add a new student record")
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody StudentCreateRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ------------------------------------------------------------------
    // 2. Update Student
    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Update an existing student record")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    // ------------------------------------------------------------------
    // 3. Get Single Student
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get a student record by ID")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }

    // ------------------------------------------------------------------
    // 4. Delete Student (Soft Delete)
    // ------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Soft delete a student by ID (sets status to INACTIVE without removing the record)")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // 5. View Students in Tables with Filters & Metrics
    // ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "View students in table with filters (search, class, section, status, gender) and counts")
    public ResponseEntity<StudentListResponse> listStudents(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Gender gender,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        StudentListResponse response = studentService.listStudents(
                schoolId, search, className, section, status, gender, pageable);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // 6. Export Students to Excel (.xlsx)
    // ------------------------------------------------------------------

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Download / export all students in Excel (.xlsx) format")
    public ResponseEntity<byte[]> exportStudents(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Gender gender) {

        byte[] data = studentService.exportStudentsToExcel(
                schoolId, search, className, section, status, gender);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("students.xlsx").build());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // 7. Bulk Upload Students from Excel (.xlsx / .xls)
    // ------------------------------------------------------------------

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Bulk upload students from an Excel (.xlsx / .xls) file")
    public ResponseEntity<StudentBulkUploadResponse> bulkUpload(
            @RequestParam(required = false) Long schoolId,
            @RequestParam("file") MultipartFile file) {
        StudentBulkUploadResponse response = studentService.bulkUploadStudents(schoolId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

