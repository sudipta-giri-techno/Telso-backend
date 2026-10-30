package com.example.app.controller;

import com.example.app.dto.request.ClassroomOperatorCreateRequest;
import com.example.app.dto.request.ClassroomOperatorUpdateRequest;
import com.example.app.dto.response.ClassroomOperatorBulkUploadResponse;
import com.example.app.dto.response.ClassroomOperatorListResponse;
import com.example.app.dto.response.ClassroomOperatorResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.ClassroomOperatorService;
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
@RequestMapping("/api/classroom-operators")
@RequiredArgsConstructor
@Tag(name = "Classroom Operators", description = "APIs for managing classroom operators, bulk upload, Excel export, and status metrics")
public class ClassroomOperatorController {

    private final ClassroomOperatorService classroomOperatorService;

    // ------------------------------------------------------------------
    // 1. Create Classroom Operator
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Add a new classroom operator (operatorName, email, phone, assignedClassroom, status, lastLogin)")
    public ResponseEntity<ClassroomOperatorResponse> createClassroomOperator(
            @Valid @RequestBody ClassroomOperatorCreateRequest request) {
        ClassroomOperatorResponse response = classroomOperatorService.createClassroomOperator(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ------------------------------------------------------------------
    // 2. Update Classroom Operator
    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Update classroom operator details or assigned classroom")
    public ResponseEntity<ClassroomOperatorResponse> updateClassroomOperator(
            @PathVariable Long id,
            @Valid @RequestBody ClassroomOperatorUpdateRequest request) {
        return ResponseEntity.ok(classroomOperatorService.updateClassroomOperator(id, request));
    }

    // ------------------------------------------------------------------
    // 3. Get single Classroom Operator
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get a classroom operator by ID")
    public ResponseEntity<ClassroomOperatorResponse> getClassroomOperator(@PathVariable Long id) {
        return ResponseEntity.ok(classroomOperatorService.getClassroomOperator(id));
    }

    // ------------------------------------------------------------------
    // 4. Delete Classroom Operator (Soft Delete)
    // ------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Soft delete a classroom operator by ID (sets status to INACTIVE without removing the record)")
    public ResponseEntity<Void> deleteClassroomOperator(@PathVariable Long id) {
        classroomOperatorService.deleteClassroomOperator(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // 5. View Classroom Operators in tables with filters + live KPI status metrics
    // ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "View classroom operators in table with filters (search, status, assigned classroom) and live status metrics (total, active, recently added, assigned classrooms, inactive)")
    public ResponseEntity<ClassroomOperatorListResponse> listClassroomOperators(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String assignedClassroom,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        ClassroomOperatorListResponse response = classroomOperatorService.listClassroomOperators(
                schoolId, search, status, assignedClassroom, pageable);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // 6. Download / Export all Classroom Operators in Excel (.xlsx) format
    // ------------------------------------------------------------------

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Download / export of all classroom operators in Excel (.xlsx) format")
    public ResponseEntity<byte[]> exportClassroomOperators(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String assignedClassroom) {

        byte[] data = classroomOperatorService.exportClassroomOperatorsToExcel(
                schoolId, search, status, assignedClassroom);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("classroom_operators.xlsx").build());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // 7. Download Excel Template for Bulk Upload
    // ------------------------------------------------------------------

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Download empty/sample Excel template for bulk upload")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] data = classroomOperatorService.downloadTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("classroom_operators_template.xlsx").build());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // 8. Bulk upload Classroom Operators from Excel (.xlsx / .xls)
    // ------------------------------------------------------------------

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Bulk upload classroom operators from an Excel (.xlsx / .xls) file")
    public ResponseEntity<ClassroomOperatorBulkUploadResponse> bulkUpload(
            @RequestParam(required = false) Long schoolId,
            @RequestParam("file") MultipartFile file) {
        ClassroomOperatorBulkUploadResponse response =
                classroomOperatorService.bulkUploadClassroomOperators(schoolId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

