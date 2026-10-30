package com.example.app.service.impl;

import com.example.app.dto.request.SectionCreateRequest;
import com.example.app.dto.request.SectionUpdateRequest;
import com.example.app.dto.response.SectionListResponse;
import com.example.app.dto.response.SectionResponse;
import com.example.app.entity.School;
import com.example.app.entity.Section;
import com.example.app.enums.UserStatus;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.SchoolRepository;
import com.example.app.repository.SectionRepository;
import com.example.app.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public SectionResponse createSection(SectionCreateRequest request) {
        School school = resolveSchool(request.getSchoolId());

        String classNum = request.getClassNumeric().trim();
        String className = request.getClassName() != null && !request.getClassName().isBlank()
                ? request.getClassName().trim()
                : ("Class " + classNum);

        Section section = Section.builder()
                .sectionName(request.getSectionName() != null ? request.getSectionName().trim() : request.getName())
                .classNumeric(classNum)
                .className(className)
                .totalStudents(request.getTotalStudents() != null ? request.getTotalStudents() : 0)
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .school(school)
                .build();

        return toResponse(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public SectionResponse updateSection(Long id, SectionUpdateRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));

        if (request.getSectionName() != null && !request.getSectionName().isBlank()) {
            section.setSectionName(request.getSectionName().trim());
        } else if (request.getName() != null && !request.getName().isBlank()) {
            section.setSectionName(request.getName().trim());
        }

        if (request.getClassNumeric() != null && !request.getClassNumeric().isBlank()) {
            section.setClassNumeric(request.getClassNumeric().trim());
        }

        if (request.getClassName() != null && !request.getClassName().isBlank()) {
            section.setClassName(request.getClassName().trim());
        }

        if (request.getTotalStudents() != null) {
            section.setTotalStudents(request.getTotalStudents());
        }

        if (request.getStatus() != null) {
            section.setStatus(request.getStatus());
        }

        if (request.getSchoolId() != null) {
            section.setSchool(resolveSchool(request.getSchoolId()));
        }

        return toResponse(sectionRepository.save(section));
    }

    @Override
    @Transactional(readOnly = true)
    public SectionResponse getSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        return toResponse(section);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        section.setStatus(UserStatus.INACTIVE);
        sectionRepository.save(section);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionListResponse listSections(Long schoolId, String search, String className, UserStatus status, Pageable pageable) {
        Page<Section> page = sectionRepository.findWithFilters(
                schoolId,
                blankToNull(search),
                blankToNull(className),
                status,
                pageable
        );

        List<SectionResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        long totalSections = sectionRepository.countTotal(schoolId);
        long activeSections = sectionRepository.countByStatus(schoolId, UserStatus.ACTIVE);
        long totalStudents = sectionRepository.sumTotalStudents(schoolId);
        double avgStudents = totalSections > 0 ? Math.round((totalStudents * 10.0) / totalSections) / 10.0 : 0.0;

        return SectionListResponse.builder()
                .sections(content)
                .content(content)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .totalSections(totalSections)
                .activeSections(activeSections)
                .totalStudents(totalStudents)
                .averageStudents(avgStudents)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportSectionsToExcel(Long schoolId, String search, String className, UserStatus status) {
        List<Section> sections = sectionRepository.findAllForExport(
                schoolId,
                blankToNull(search),
                blankToNull(className),
                status
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Sections");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"#", "Section Name", "Class", "Class Name", "Total Students", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Section s : sections) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(s.getSectionName());
                row.createCell(2).setCellValue(s.getClassNumeric());
                row.createCell(3).setCellValue(s.getClassName());
                row.createCell(4).setCellValue(s.getTotalStudents() != null ? s.getTotalStudents() : 0);
                row.createCell(5).setCellValue(s.getStatus() != null ? s.getStatus().name() : UserStatus.ACTIVE.name());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export for sections", e);
        }
    }

    private SectionResponse toResponse(Section s) {
        String badge = "";
        if (s.getSectionName() != null && !s.getSectionName().isBlank()) {
            String[] parts = s.getSectionName().trim().split("\\s+");
            badge = parts[parts.length - 1].toUpperCase();
        }

        return SectionResponse.builder()
                .id(s.getId())
                .sectionName(s.getSectionName())
                .name(s.getSectionName())
                .badge(badge)
                .classNumeric(s.getClassNumeric())
                .className(s.getClassName())
                .totalStudents(s.getTotalStudents())
                .status(s.getStatus())
                .schoolId(s.getSchool() != null ? s.getSchool().getId() : null)
                .schoolName(s.getSchool() != null ? s.getSchool().getSchoolName() : null)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private School resolveSchool(Long schoolId) {
        if (schoolId == null) return null;
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

