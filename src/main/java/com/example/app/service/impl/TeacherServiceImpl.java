package com.example.app.service.impl;

import com.example.app.dto.request.TeacherCreateRequest;
import com.example.app.dto.request.TeacherUpdateRequest;
import com.example.app.dto.response.TeacherListResponse;
import com.example.app.dto.response.TeacherResponse;
import com.example.app.entity.School;
import com.example.app.entity.Teacher;
import com.example.app.enums.UserStatus;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.SchoolRepository;
import com.example.app.repository.TeacherRepository;
import com.example.app.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final SchoolRepository  schoolRepository;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public TeacherResponse createTeacher(TeacherCreateRequest request) {
        if (teacherRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Teacher with email already exists: " + request.getEmail());
        }

        School school = resolveSchool(request.getSchoolId());

        Teacher teacher = Teacher.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .classSections(request.getClassSections() != null
                        ? new ArrayList<>(request.getClassSections())
                        : new ArrayList<>())
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .school(school)
                .build();

        return toResponse(teacherRepository.save(teacher));
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        if (request.getName()    != null) teacher.setName(request.getName());
        if (request.getPhone()   != null) teacher.setPhone(request.getPhone());
        if (request.getSubject() != null) teacher.setSubject(request.getSubject());
        if (request.getStatus()  != null) teacher.setStatus(request.getStatus());

        // Email uniqueness check on change
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(teacher.getEmail())) {
            if (teacherRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            teacher.setEmail(request.getEmail());
        }

        // Full replacement of class-section list
        if (request.getClassSections() != null) {
            teacher.getClassSections().clear();
            teacher.getClassSections().addAll(request.getClassSections());
        }

        return toResponse(teacherRepository.save(teacher));
    }

    // -------------------------------------------------------------------------
    // Get single
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse getTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        return toResponse(teacher);
    }

    // -------------------------------------------------------------------------
    // List with filters
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TeacherListResponse listTeachers(
            Long schoolId,
            String search,
            String subject,
            String classSection,
            UserStatus status,
            Pageable pageable) {

        Page<Teacher> page = teacherRepository.findWithFilters(
                schoolId,
                blankToNull(search),
                blankToNull(subject),
                blankToNull(classSection),
                status,
                pageable
        );

        List<TeacherResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return TeacherListResponse.builder()
                .totalTeachers(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .teachers(content)
                .build();
    }

    // -------------------------------------------------------------------------
    // Excel Export
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTeachersToExcel(
            Long schoolId,
            String search,
            String subject,
            String classSection,
            UserStatus status) {

        List<Teacher> teachers = teacherRepository.findAllForExport(
                schoolId,
                blankToNull(search),
                blankToNull(subject),
                blankToNull(classSection),
                status
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Teachers");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row header = sheet.createRow(0);
            String[] columns = {"#", "Name", "Email", "Phone", "Subject", "Classes", "Status", "Last Login"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Teacher t : teachers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(t.getName());
                row.createCell(2).setCellValue(t.getEmail());
                row.createCell(3).setCellValue(t.getPhone() != null ? t.getPhone() : "");
                row.createCell(4).setCellValue(t.getSubject() != null ? t.getSubject() : "");
                row.createCell(5).setCellValue(String.join(", ", t.getClassSections()));
                row.createCell(6).setCellValue(t.getStatus().name());
                row.createCell(7).setCellValue(
                        t.getLastLogin() != null ? t.getLastLogin().toString() : ""
                );
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    // -------------------------------------------------------------------------
    // Bulk Upload from Excel
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public TeacherListResponse bulkUploadTeachers(Long schoolId, MultipartFile file) {
        School school = resolveSchool(schoolId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        List<Teacher> created = new ArrayList<>();
        List<String> skipped  = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Row 0 is the header – start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name    = getCellString(row, 0);
                String email   = getCellString(row, 1);
                String phone   = getCellString(row, 2);
                String subject = getCellString(row, 3);
                // Classes column: "7B, 8A, 9C"
                String classesRaw = getCellString(row, 4);
                String statusStr  = getCellString(row, 6);

                if (name.isBlank() || email.isBlank()) continue;

                if (teacherRepository.existsByEmail(email)) {
                    skipped.add(email);
                    continue;
                }

                List<String> sections = parseClassSections(classesRaw);
                UserStatus status = parseStatus(statusStr);

                Teacher teacher = Teacher.builder()
                        .name(name)
                        .email(email)
                        .phone(phone)
                        .subject(subject)
                        .classSections(sections)
                        .status(status)
                        .school(school)
                        .build();

                created.add(teacherRepository.save(teacher));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse uploaded Excel file: " + e.getMessage(), e);
        }

        List<TeacherResponse> responses = created.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return TeacherListResponse.builder()
                .totalTeachers(created.size())
                .totalPages(1)
                .currentPage(0)
                .pageSize(created.size())
                .teachers(responses)
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private TeacherResponse toResponse(Teacher t) {
        return TeacherResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .email(t.getEmail())
                .phone(t.getPhone())
                .subject(t.getSubject())
                .classSections(t.getClassSections())
                .status(t.getStatus())
                .lastLogin(t.getLastLogin())
                .schoolId(t.getSchool() != null ? t.getSchool().getId() : null)
                .schoolName(t.getSchool() != null ? t.getSchool().getSchoolName() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private School resolveSchool(Long schoolId) {
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId is required");
        }
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private List<String> parseClassSections(String raw) {
        if (raw == null || raw.isBlank()) return new ArrayList<>();
        return Arrays.stream(raw.split("[,;]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private UserStatus parseStatus(String s) {
        if (s == null) return UserStatus.ACTIVE;
        try {
            return UserStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            // "Active" / "Inactive" from the template
            return s.equalsIgnoreCase("inactive") ? UserStatus.INACTIVE : UserStatus.ACTIVE;
        }
    }
}
