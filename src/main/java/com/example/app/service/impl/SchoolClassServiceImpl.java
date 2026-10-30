package com.example.app.service.impl;

import com.example.app.dto.request.SchoolClassCreateRequest;
import com.example.app.dto.request.SchoolClassUpdateRequest;
import com.example.app.dto.response.SchoolClassListResponse;
import com.example.app.dto.response.SchoolClassResponse;
import com.example.app.entity.School;
import com.example.app.entity.SchoolClass;
import com.example.app.enums.UserStatus;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.SchoolClassRepository;
import com.example.app.repository.SchoolRepository;
import com.example.app.service.SchoolClassService;
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
public class SchoolClassServiceImpl implements SchoolClassService {

    private final SchoolClassRepository classRepository;
    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public SchoolClassResponse createClass(SchoolClassCreateRequest request) {
        School school = resolveSchool(request.getSchoolId());

        SchoolClass sc = SchoolClass.builder()
                .className(request.getClassName() != null ? request.getClassName().trim() : request.getName())
                .gradeLevel(blankToNull(request.getGradeLevel()))
                .totalSections(request.getTotalSections() != null ? request.getTotalSections() : 0)
                .totalStudents(request.getTotalStudents() != null ? request.getTotalStudents() : 0)
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .school(school)
                .build();

        return toResponse(classRepository.save(sc));
    }

    @Override
    @Transactional
    public SchoolClassResponse updateClass(Long id, SchoolClassUpdateRequest request) {
        SchoolClass sc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        if (request.getClassName() != null && !request.getClassName().isBlank()) {
            sc.setClassName(request.getClassName().trim());
        } else if (request.getName() != null && !request.getName().isBlank()) {
            sc.setClassName(request.getName().trim());
        }

        if (request.getGradeLevel() != null) {
            sc.setGradeLevel(blankToNull(request.getGradeLevel()));
        }

        if (request.getTotalSections() != null) {
            sc.setTotalSections(request.getTotalSections());
        }

        if (request.getTotalStudents() != null) {
            sc.setTotalStudents(request.getTotalStudents());
        }

        if (request.getStatus() != null) {
            sc.setStatus(request.getStatus());
        }

        if (request.getSchoolId() != null) {
            sc.setSchool(resolveSchool(request.getSchoolId()));
        }

        return toResponse(classRepository.save(sc));
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClassResponse getClass(Long id) {
        SchoolClass sc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));
        return toResponse(sc);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        SchoolClass sc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));
        sc.setStatus(UserStatus.INACTIVE);
        classRepository.save(sc);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClassListResponse listClasses(Long schoolId, String search, UserStatus status, Pageable pageable) {
        Page<SchoolClass> page = classRepository.findWithFilters(
                schoolId,
                blankToNull(search),
                status,
                pageable
        );

        List<SchoolClassResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        long totalClasses = classRepository.countTotal(schoolId);
        long totalSections = classRepository.sumTotalSections(schoolId);
        long totalStudents = classRepository.sumTotalStudents(schoolId);
        double avgSections = totalClasses > 0 ? Math.round((totalSections * 10.0) / totalClasses) / 10.0 : 0.0;

        return SchoolClassListResponse.builder()
                .classes(content)
                .content(content)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .totalClasses(totalClasses)
                .totalSections(totalSections)
                .totalStudents(totalStudents)
                .averageSections(avgSections)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportClassesToExcel(Long schoolId, String search, UserStatus status) {
        List<SchoolClass> classes = classRepository.findAllForExport(
                schoolId,
                blankToNull(search),
                status
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Classes");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"#", "Class Name", "Grade / Level", "Total Sections", "Total Students", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (SchoolClass sc : classes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(sc.getClassName());
                row.createCell(2).setCellValue(sc.getGradeLevel() != null ? sc.getGradeLevel() : "");
                row.createCell(3).setCellValue(sc.getTotalSections() != null ? sc.getTotalSections() : 0);
                row.createCell(4).setCellValue(sc.getTotalStudents() != null ? sc.getTotalStudents() : 0);
                row.createCell(5).setCellValue(sc.getStatus() != null ? sc.getStatus().name() : UserStatus.ACTIVE.name());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export for classes", e);
        }
    }

    private SchoolClassResponse toResponse(SchoolClass sc) {
        return SchoolClassResponse.builder()
                .id(sc.getId())
                .className(sc.getClassName())
                .name(sc.getClassName())
                .gradeLevel(sc.getGradeLevel())
                .totalSections(sc.getTotalSections())
                .totalStudents(sc.getTotalStudents())
                .status(sc.getStatus())
                .schoolId(sc.getSchool() != null ? sc.getSchool().getId() : null)
                .schoolName(sc.getSchool() != null ? sc.getSchool().getSchoolName() : null)
                .createdAt(sc.getCreatedAt())
                .updatedAt(sc.getUpdatedAt())
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

