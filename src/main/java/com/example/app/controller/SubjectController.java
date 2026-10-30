package com.example.app.controller;

import com.example.app.dto.request.SubjectCreateRequest;
import com.example.app.dto.request.SubjectUpdateRequest;
import com.example.app.dto.response.SubjectListResponse;
import com.example.app.dto.response.SubjectResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.SubjectService;
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
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Subject management APIs (create, update, view, export)")
public class SubjectController {

    private final SubjectService subjectService;

    // ------------------------------------------------------------------
    // 1. Create Subject
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Create a new subject (subjectName, classes, status)")
    public ResponseEntity<SubjectResponse> createSubject(
            @Valid @RequestBody SubjectCreateRequest request) {
        SubjectResponse response = subjectService.createSubject(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ------------------------------------------------------------------
    // 2. Export Subjects to Excel
    // ------------------------------------------------------------------

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Download / export subjects list in Excel (.xlsx) format")
    public ResponseEntity<byte[]> exportSubjects(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) UserStatus status) {

        byte[] data = subjectService.exportSubjectsToExcel(schoolId, search, className, status);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("subjects.xlsx").build());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // 3. View Subjects in Table with Filters & Metrics
    // ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "View all subjects with filters (search, select class, status) and totalSubjects count")
    public ResponseEntity<SubjectListResponse> listSubjects(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        SubjectListResponse response = subjectService.listSubjects(schoolId, search, className, status, pageable);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // 4. Get Subject by ID
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get a subject record by ID")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubject(id));
    }

    // ------------------------------------------------------------------
    // 5. Update Subject
    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Update an existing subject record")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectUpdateRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }

    // ------------------------------------------------------------------
    // 6. Delete Subject (Soft Delete)
    // ------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Soft delete a subject by ID (sets status to INACTIVE)")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}

