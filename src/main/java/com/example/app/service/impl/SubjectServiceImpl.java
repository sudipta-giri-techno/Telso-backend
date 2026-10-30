package com.example.app.service.impl;

import com.example.app.dto.request.SubjectCreateRequest;
import com.example.app.dto.request.SubjectUpdateRequest;
import com.example.app.dto.response.SubjectListResponse;
import com.example.app.dto.response.SubjectResponse;
import com.example.app.entity.School;
import com.example.app.entity.Subject;
import com.example.app.enums.UserStatus;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.SchoolRepository;
import com.example.app.repository.SubjectRepository;
import com.example.app.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public SubjectResponse createSubject(SubjectCreateRequest request) {
        School school = resolveSchool(request.getSchoolId());

        Subject subject = Subject.builder()
                .subjectName(request.getSubjectName() != null ? request.getSubjectName().trim() : request.getName())
                .classes(request.getClasses() != null ? new ArrayList<>(request.getClasses()) : new ArrayList<>())
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .school(school)
                .build();

        return toResponse(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectUpdateRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        if (request.getSubjectName() != null && !request.getSubjectName().isBlank()) {
            subject.setSubjectName(request.getSubjectName().trim());
        } else if (request.getName() != null && !request.getName().isBlank()) {
            subject.setSubjectName(request.getName().trim());
        }

        if (request.getClasses() != null) {
            subject.getClasses().clear();
            subject.getClasses().addAll(request.getClasses());
        }

        if (request.getStatus() != null) {
            subject.setStatus(request.getStatus());
        }

        if (request.getSchoolId() != null) {
            subject.setSchool(resolveSchool(request.getSchoolId()));
        }

        return toResponse(subjectRepository.save(subject));
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return toResponse(subject);
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        subject.setStatus(UserStatus.INACTIVE);
        subjectRepository.save(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectListResponse listSubjects(Long schoolId, String search, String className, UserStatus status, Pageable pageable) {
        Page<Subject> page = subjectRepository.findWithFilters(
                schoolId,
                blankToNull(search),
                blankToNull(className),
                status,
                pageable
        );

        List<SubjectResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        long totalSubjects = subjectRepository.countTotal(schoolId);

        return SubjectListResponse.builder()
                .subjects(content)
                .content(content)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .totalSubjects(totalSubjects)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportSubjectsToExcel(Long schoolId, String search, String className, UserStatus status) {
        List<Subject> subjects = subjectRepository.findAllForExport(
                schoolId,
                blankToNull(search),
                blankToNull(className),
                status
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Subjects");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"#", "Subject Name", "Classes", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Subject s : subjects) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(s.getSubjectName());
                row.createCell(2).setCellValue(String.join(", ", s.getClasses()));
                row.createCell(3).setCellValue(s.getStatus() != null ? s.getStatus().name() : UserStatus.ACTIVE.name());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export for subjects", e);
        }
    }

    private SubjectResponse toResponse(Subject s) {
        return SubjectResponse.builder()
                .id(s.getId())
                .subjectName(s.getSubjectName())
                .name(s.getSubjectName())
                .classes(s.getClasses() != null ? new ArrayList<>(s.getClasses()) : new ArrayList<>())
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

