package com.example.app.controller;

import com.example.app.dto.request.SectionCreateRequest;
import com.example.app.dto.request.SectionUpdateRequest;
import com.example.app.dto.response.SectionListResponse;
import com.example.app.dto.response.SectionResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.SectionService;
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
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(name = "Sections", description = "Section management APIs (create, update, view, export)")
public class SectionController {

    private final SectionService sectionService;

    // ------------------------------------------------------------------
    // 1. Create Section
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Create a new section (sectionName, classNumeric, className, totalStudents, status)")
    public ResponseEntity<SectionResponse> createSection(
            @Valid @RequestBody SectionCreateRequest request) {
        SectionResponse response = sectionService.createSection(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ------------------------------------------------------------------
    // 2. Export Sections to Excel
    // ------------------------------------------------------------------

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Download / export sections list in Excel (.xlsx) format")
    public ResponseEntity<byte[]> exportSections(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) UserStatus status) {

        byte[] data = sectionService.exportSectionsToExcel(schoolId, search, className, status);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("sections.xlsx").build());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // 3. View Sections in Table with Filters & KPI metrics
    // ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "View all sections with filters and summary metrics (totalSections, activeSections, totalStudents, averageStudents)")
    public ResponseEntity<SectionListResponse> listSections(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        SectionListResponse response = sectionService.listSections(schoolId, search, className, status, pageable);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // 4. Get Section by ID
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Get a section record by ID")
    public ResponseEntity<SectionResponse> getSection(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSection(id));
    }

    // ------------------------------------------------------------------
    // 5. Update Section
    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Update an existing section record")
    public ResponseEntity<SectionResponse> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody SectionUpdateRequest request) {
        return ResponseEntity.ok(sectionService.updateSection(id, request));
    }

    // ------------------------------------------------------------------
    // 6. Delete Section (Soft Delete)
    // ------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'CENTRAL_ADMIN')")
    @Operation(summary = "Soft delete a section by ID (sets status to INACTIVE)")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}

