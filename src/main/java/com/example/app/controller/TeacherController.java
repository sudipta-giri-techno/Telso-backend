package com.example.app.controller;

import com.example.app.dto.request.TeacherCreateRequest;
import com.example.app.dto.request.TeacherUpdateRequest;
import com.example.app.dto.response.TeacherListResponse;
import com.example.app.dto.response.TeacherResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.TeacherService;
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
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Teacher management APIs for school admins")
public class TeacherController {

    private final TeacherService teacherService;

    // ------------------------------------------------------------------
    // 1. Create teacher
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @Operation(summary = "Add a new teacher (school admin only)")
    public ResponseEntity<TeacherResponse> createTeacher(
            @Valid @RequestBody TeacherCreateRequest request) {
        TeacherResponse response = teacherService.createTeacher(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ------------------------------------------------------------------
    // 2. Update teacher
    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @Operation(summary = "Update teacher details and/or class-section assignments")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateRequest request) {
        return ResponseEntity.ok(teacherService.updateTeacher(id, request));
    }

    // ------------------------------------------------------------------
    // 3. Get single teacher
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get a teacher by ID")
    public ResponseEntity<TeacherResponse> getTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacher(id));
    }

    // ------------------------------------------------------------------
    // 4. List teachers with filters + total count
    // ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "List teachers with filters (search, subject, class, status) and total count")
    public ResponseEntity<TeacherListResponse> listTeachers(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String classSection,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        TeacherListResponse response = teacherService.listTeachers(
                schoolId, search, subject, classSection, status, pageable);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // 5. Export all teachers as Excel
    // ------------------------------------------------------------------

    @GetMapping("/export")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @Operation(summary = "Export teachers list as Excel (.xlsx)")
    public ResponseEntity<byte[]> exportTeachers(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String classSection,
            @RequestParam(required = false) UserStatus status) {

        byte[] data = teacherService.exportTeachersToExcel(schoolId, search, subject, classSection, status);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("teachers.xlsx").build());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // 6. Bulk upload teachers from Excel
    // ------------------------------------------------------------------

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @Operation(summary = "Bulk upload teachers from an Excel file")
    public ResponseEntity<TeacherListResponse> bulkUpload(
            @RequestParam Long schoolId,
            @RequestParam("file") MultipartFile file) {
        TeacherListResponse response = teacherService.bulkUploadTeachers(schoolId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
