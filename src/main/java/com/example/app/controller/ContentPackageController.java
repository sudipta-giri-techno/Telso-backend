package com.example.app.controller;

import com.example.app.dto.request.InstallPackageRequest;
import com.example.app.dto.response.ContentPackageListResponse;
import com.example.app.dto.response.ContentPackageResponse;
import com.example.app.dto.response.ContentPackageStatsResponse;
import com.example.app.enums.ContentInstallationStatus;
import com.example.app.service.ContentPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Content Packages", description = "View and manage content packages for classroom operators")
@RestController
@RequestMapping("/api/content-packages")
@RequiredArgsConstructor
public class ContentPackageController {

    private final ContentPackageService contentPackageService;

    // 1. List all packages with KPI stats + filters
    @Operation(summary = "List content packages for a classroom operator",
               description = "Returns KPI cards (total, installed, downloading, not-installed) and a paginated, filterable table of packages.")
    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','CENTRAL_ADMIN','CONTENT_ADMIN')")
    public ResponseEntity<ContentPackageListResponse> listPackages(
            @Parameter(description = "Classroom operator ID", required = true)
            @RequestParam Long operatorId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) ContentInstallationStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        ContentPackageListResponse response = contentPackageService.listPackages(
                operatorId, search, className, subject, language, status, pageable);
        return ResponseEntity.ok(response);
    }

    // 2. Get single package detail
    @Operation(summary = "Get a single content package detail for an operator")
    @GetMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','CENTRAL_ADMIN','CONTENT_ADMIN')")
    public ResponseEntity<ContentPackageResponse> getPackage(
            @RequestParam Long operatorId,
            @PathVariable Long contentId
    ) {
        return ResponseEntity.ok(contentPackageService.getPackage(operatorId, contentId));
    }

    // 3. Trigger install (DOWNLOADING -> INSTALLED state machine)
    @Operation(summary = "Install / trigger download of a content package",
               description = "First call sets status to DOWNLOADING; second call sets status to INSTALLED.")
    @PostMapping("/{contentId}/install")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','CENTRAL_ADMIN')")
    public ResponseEntity<ContentPackageResponse> installPackage(
            @PathVariable Long contentId,
            @Valid @RequestBody InstallPackageRequest request
    ) {
        return ResponseEntity.ok(
                contentPackageService.installPackage(request.getOperatorId(), contentId));
    }

    // 4. KPI stats only
    @Operation(summary = "Get KPI stats for content packages of an operator")
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','CENTRAL_ADMIN','CONTENT_ADMIN')")
    public ResponseEntity<ContentPackageStatsResponse> getStats(
            @RequestParam Long operatorId
    ) {
        return ResponseEntity.ok(contentPackageService.getStats(operatorId));
    }

    // 5. Export packages to Excel
    @Operation(summary = "Export content packages to Excel (.xlsx)")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','CENTRAL_ADMIN','CONTENT_ADMIN')")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam Long operatorId
    ) throws Exception {
        byte[] excelBytes = contentPackageService.exportPackagesToExcel(operatorId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"content-packages.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}

