package com.example.app.controller;

import com.example.app.dto.request.SchoolClassCreateRequest;
import com.example.app.dto.request.SchoolClassUpdateRequest;
import com.example.app.dto.response.SchoolClassListResponse;
import com.example.app.dto.response.SchoolClassResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.SchoolClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Class management APIs (create, update, view, export)")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    // ------------------------------------------------------------------
    // 1. Create Class
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Create a new class (className, gradeLevel, totalSections, totalStudents, status)")
    public ResponseEntity<SchoolClassResponse> createClass(
            @Valid @RequestBody SchoolClassCreateRequest request) {
        SchoolClassResponse response = schoolClassService.createClass(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ------------------------------------------------------------------
    // 2. Export Classes to Excel
    // ------------------------------------------------------------------

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Download / export classes list in Excel (.xlsx) format")
    public ResponseEntity<byte[]> exportClasses(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status) {

        byte[] data = schoolClassService.exportClassesToExcel(schoolId, search, status);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("classes.xlsx").build());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // 3. View Classes in Table with Filters & KPI metrics
    // ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "View all classes with filters and summary metrics (totalClasses, totalSections, totalStudents, averageSections)")
    public ResponseEntity<SchoolClassListResponse> listClasses(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        SchoolClassListResponse response = schoolClassService.listClasses(schoolId, search, status, pageable);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // 4. Get Class by ID
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get a class record by ID")
    public ResponseEntity<SchoolClassResponse> getClass(@PathVariable Long id) {
        return ResponseEntity.ok(schoolClassService.getClass(id));
    }

    // ------------------------------------------------------------------
    // 5. Update Class
    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Update an existing class record")
    public ResponseEntity<SchoolClassResponse> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody SchoolClassUpdateRequest request) {
        return ResponseEntity.ok(schoolClassService.updateClass(id, request));
    }

    // ------------------------------------------------------------------
    // 6. Delete Class (Soft Delete)
    // ------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Soft delete a class by ID (sets status to INACTIVE)")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        schoolClassService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}

