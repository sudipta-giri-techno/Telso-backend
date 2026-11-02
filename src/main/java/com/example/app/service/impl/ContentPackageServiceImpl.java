package com.example.app.service.impl;

import com.example.app.dto.response.*;
import com.example.app.entity.Content;
import com.example.app.entity.ContentInstallation;
import com.example.app.entity.ClassroomOperator;
import com.example.app.enums.ContentInstallationStatus;
import com.example.app.repository.ClassroomOperatorRepository;
import com.example.app.repository.ContentInstallationRepository;
import com.example.app.repository.ContentRepository;
import com.example.app.service.ContentPackageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentPackageServiceImpl implements ContentPackageService {

    private final ContentRepository contentRepository;
    private final ContentInstallationRepository installationRepository;
    private final ClassroomOperatorRepository operatorRepository;

    // ── List packages with filters ─────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ContentPackageListResponse listPackages(
            Long operatorId,
            String search,
            String className,
            String subject,
            String language,
            ContentInstallationStatus filterStatus,
            Pageable pageable) {

        // Validate operator
        ClassroomOperator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom operator not found: " + operatorId));

        // Load all active content (filtered by search / class / subject)
        Page<Content> contentPage = contentRepository.findActiveContents(
                emptyToNull(search), emptyToNull(className), emptyToNull(subject), pageable);

        // Load operator's existing installation records as a map contentId → installation
        Map<Long, ContentInstallation> installMap = installationRepository
                .findByOperatorId(operatorId)
                .stream()
                .collect(Collectors.toMap(ci -> ci.getContent().getId(), ci -> ci));

        // Map to response DTOs
        List<ContentPackageResponse> responses = contentPage.getContent().stream()
                .map(c -> {
                    ContentInstallation ci = installMap.get(c.getId());
                    ContentInstallationStatus status = ci != null ? ci.getStatus()
                            : ContentInstallationStatus.NOT_INSTALLED;
                    return buildResponse(c, ci, status);
                })
                // filter by language if specified
                .filter(r -> language == null || language.isBlank()
                        || (r.getLanguages() != null && r.getLanguages().stream()
                                .anyMatch(l -> l.equalsIgnoreCase(language))))
                // filter by install status if specified
                .filter(r -> filterStatus == null || r.getInstallStatus() == filterStatus)
                .collect(Collectors.toList());

        // KPI stats
        ContentPackageStatsResponse stats = buildStats(operatorId, contentPage.getTotalElements());

        return ContentPackageListResponse.builder()
                .stats(stats)
                .packages(responses)
                .page(contentPage.getNumber())
                .size(contentPage.getSize())
                .totalElements(contentPage.getTotalElements())
                .totalPages(contentPage.getTotalPages())
                .build();
    }

    // ── Get single package ─────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ContentPackageResponse getPackage(Long operatorId, Long contentId) {
        operatorRepository.findById(operatorId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom operator not found: " + operatorId));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Content not found: " + contentId));

        Optional<ContentInstallation> ci = installationRepository
                .findByOperatorIdAndContentId(operatorId, contentId);

        ContentInstallationStatus status = ci.map(ContentInstallation::getStatus)
                .orElse(ContentInstallationStatus.NOT_INSTALLED);

        return buildResponse(content, ci.orElse(null), status);
    }

    // ── Install a package ──────────────────────────────────────────────────
    @Override
    @Transactional
    public ContentPackageResponse installPackage(Long operatorId, Long contentId) {
        ClassroomOperator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom operator not found: " + operatorId));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Content not found: " + contentId));

        ContentInstallation ci = installationRepository
                .findByOperatorIdAndContentId(operatorId, contentId)
                .orElseGet(() -> ContentInstallation.builder()
                        .operator(operator)
                        .content(content)
                        .build());

        // Transition state machine
        ContentInstallationStatus nextStatus;
        if (ci.getStatus() == null || ci.getStatus() == ContentInstallationStatus.NOT_INSTALLED) {
            nextStatus = ContentInstallationStatus.DOWNLOADING;
        } else if (ci.getStatus() == ContentInstallationStatus.DOWNLOADING) {
            nextStatus = ContentInstallationStatus.INSTALLED;
            ci.setInstalledAt(LocalDateTime.now());
        } else {
            nextStatus = ci.getStatus(); // already INSTALLED or AVAILABLE
        }
        ci.setStatus(nextStatus);
        ContentInstallation saved = installationRepository.save(ci);

        return buildResponse(content, saved, nextStatus);
    }

    // ── Stats ──────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ContentPackageStatsResponse getStats(Long operatorId) {
        long total = contentRepository.countByActiveTrue();
        return buildStats(operatorId, total);
    }

    // ── Excel export ───────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public byte[] exportPackagesToExcel(Long operatorId) throws Exception {
        operatorRepository.findById(operatorId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom operator not found: " + operatorId));

        List<Content> contents = contentRepository.findAll()
                .stream().filter(Content::isActive).collect(Collectors.toList());

        Map<Long, ContentInstallation> installMap = installationRepository
                .findByOperatorId(operatorId)
                .stream()
                .collect(Collectors.toMap(ci -> ci.getContent().getId(), ci -> ci));

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Content Packages");

            // Header style — royal blue
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            String[] headers = {
                "#", "Package Name", "Class", "Subject", "Languages",
                "Version", "Published By", "Published On", "Install Status"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Content c : contents) {
                Row row = sheet.createRow(rowIdx);
                ContentInstallation ci = installMap.get(c.getId());
                ContentInstallationStatus status = ci != null ? ci.getStatus()
                        : ContentInstallationStatus.NOT_INSTALLED;

                row.createCell(0).setCellValue(rowIdx);
                row.createCell(1).setCellValue(c.getTitle());
                row.createCell(2).setCellValue(c.getTargetClass());
                row.createCell(3).setCellValue(c.getSubject());
                row.createCell(4).setCellValue(
                    c.getLanguages() != null ? String.join(", ", c.getLanguages()) : "");
                row.createCell(5).setCellValue(c.getVersion() != null ? c.getVersion() : "");
                row.createCell(6).setCellValue(
                    c.getUploadedBy() != null ? c.getUploadedBy().getFullName() : "");
                row.createCell(7).setCellValue(
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
                row.createCell(8).setCellValue(status.name());
                rowIdx++;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private ContentPackageResponse buildResponse(
            Content c, ContentInstallation ci, ContentInstallationStatus status) {
        String publishedBy = "Content Admin";
        if (c.getUploadedBy() != null) {
            String fullName = c.getUploadedBy().getFullName();
            publishedBy = (fullName != null && !fullName.isBlank())
                    ? fullName : c.getUploadedBy().getEmail();
        }

        return ContentPackageResponse.builder()
                .contentId(c.getId())
                .packageName(c.getTitle())
                .description(c.getDescription())
                .className(c.getTargetClass())
                .subject(c.getSubject())
                .languages(c.getLanguages())
                .version(c.getVersion())
                .publishedBy(publishedBy)
                .publishedOn(c.getCreatedAt())
                .installStatus(status)
                .installationId(ci != null ? ci.getId() : null)
                .build();
    }

    private ContentPackageStatsResponse buildStats(Long operatorId, long totalPackages) {
        long installed   = installationRepository.countByOperatorIdAndStatus(operatorId, ContentInstallationStatus.INSTALLED);
        long downloading = installationRepository.countByOperatorIdAndStatus(operatorId, ContentInstallationStatus.DOWNLOADING);
        long notInstalled = totalPackages - installed - downloading;
        if (notInstalled < 0) notInstalled = 0;
        return ContentPackageStatsResponse.builder()
                .totalPackages(totalPackages)
                .installed(installed)
                .downloading(downloading)
                .notInstalled(notInstalled)
                .build();
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
