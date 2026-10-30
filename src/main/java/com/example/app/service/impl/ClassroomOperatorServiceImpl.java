package com.example.app.service.impl;

import com.example.app.dto.request.ClassroomOperatorCreateRequest;
import com.example.app.dto.request.ClassroomOperatorUpdateRequest;
import com.example.app.dto.response.ClassroomOperatorBulkUploadResponse;
import com.example.app.dto.response.ClassroomOperatorListResponse;
import com.example.app.dto.response.ClassroomOperatorResponse;
import com.example.app.entity.ClassroomOperator;
import com.example.app.entity.School;
import com.example.app.enums.UserStatus;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ClassroomOperatorRepository;
import com.example.app.repository.SchoolRepository;
import com.example.app.service.ClassroomOperatorService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomOperatorServiceImpl implements ClassroomOperatorService {

    private final ClassroomOperatorRepository operatorRepository;
    private final SchoolRepository schoolRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    // -------------------------------------------------------------------------
    // 1. Create Classroom Operator
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ClassroomOperatorResponse createClassroomOperator(ClassroomOperatorCreateRequest request) {
        if (operatorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Classroom operator with email already exists: " + request.getEmail());
        }

        School school = resolveSchool(request.getSchoolId());

        ClassroomOperator operator = ClassroomOperator.builder()
                .operatorName(request.getOperatorName() != null ? request.getOperatorName().trim() : request.getName())
                .email(request.getEmail().trim().toLowerCase())
                .phone(blankToNull(request.getPhone()))
                .assignedClassroom(blankToNull(request.getAssignedClassroom()))
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .lastLogin(request.getLastLogin())
                .school(school)
                .build();

        return toResponse(operatorRepository.save(operator));
    }

    // -------------------------------------------------------------------------
    // Update Classroom Operator
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ClassroomOperatorResponse updateClassroomOperator(Long id, ClassroomOperatorUpdateRequest request) {
        ClassroomOperator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom operator not found with id: " + id));

        if (request.getOperatorName() != null && !request.getOperatorName().isBlank()) {
            operator.setOperatorName(request.getOperatorName().trim());
        } else if (request.getName() != null && !request.getName().isBlank()) {
            operator.setOperatorName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(operator.getEmail())) {
            if (operatorRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            operator.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getPhone() != null) {
            operator.setPhone(blankToNull(request.getPhone()));
        }

        if (request.getAssignedClassroom() != null) {
            operator.setAssignedClassroom(blankToNull(request.getAssignedClassroom()));
        }

        if (request.getStatus() != null) {
            operator.setStatus(request.getStatus());
        }

        if (request.getLastLogin() != null) {
            operator.setLastLogin(request.getLastLogin());
        }

        if (request.getSchoolId() != null) {
            operator.setSchool(resolveSchool(request.getSchoolId()));
        }

        return toResponse(operatorRepository.save(operator));
    }

    // -------------------------------------------------------------------------
    // Get single Classroom Operator
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ClassroomOperatorResponse getClassroomOperator(Long id) {
        ClassroomOperator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom operator not found with id: " + id));
        return toResponse(operator);
    }

    // -------------------------------------------------------------------------
    // Delete Classroom Operator (Soft Delete -> updates status to INACTIVE)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteClassroomOperator(Long id) {
        ClassroomOperator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom operator not found with id: " + id));
        operator.setStatus(UserStatus.INACTIVE);
        operatorRepository.save(operator);
    }

    // -------------------------------------------------------------------------
    // 4. View Classroom Operators in Table with Filters + Live Status KPI Cards
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ClassroomOperatorListResponse listClassroomOperators(
            Long schoolId,
            String search,
            UserStatus status,
            String assignedClassroom,
            Pageable pageable) {

        Page<ClassroomOperator> page = operatorRepository.findWithFilters(
                schoolId,
                blankToNull(search),
                status,
                blankToNull(assignedClassroom),
                pageable
        );

        List<ClassroomOperatorResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        // Always query live status counts for KPI summary cards
        long totalOps = operatorRepository.countTotal(schoolId);
        long activeOps = operatorRepository.countByStatus(schoolId, UserStatus.ACTIVE);
        long inactiveOps = operatorRepository.countByStatus(schoolId, UserStatus.INACTIVE);
        long recentlyAdded = operatorRepository.countRecentlyAdded(schoolId, LocalDateTime.now().minusDays(30));
        long assignedClassroomsCount = operatorRepository.countDistinctAssignedClassrooms(schoolId);

        double activePct = totalOps > 0 ? Math.round((activeOps * 1000.0) / totalOps) / 10.0 : 0.0;
        double inactivePct = totalOps > 0 ? Math.round((inactiveOps * 1000.0) / totalOps) / 10.0 : 0.0;

        return ClassroomOperatorListResponse.builder()
                .operators(content)
                .content(content)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                // KPI statistics
                .totalOperators(totalOps)
                .activeOperators(activeOps)
                .inactiveOperators(inactiveOps)
                .recentlyAdded(recentlyAdded)
                .assignedClassrooms(assignedClassroomsCount)
                .activePercentage(activePct)
                .inactivePercentage(inactivePct)
                .build();
    }

    // -------------------------------------------------------------------------
    // 3. Export all Classroom Operators to Excel (.xlsx)
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public byte[] exportClassroomOperatorsToExcel(
            Long schoolId,
            String search,
            UserStatus status,
            String assignedClassroom) {

        List<ClassroomOperator> operators = operatorRepository.findAllForExport(
                schoolId,
                blankToNull(search),
                status,
                blankToNull(assignedClassroom)
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Classroom Operators");

            // Header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Columns matching table view
            String[] headers = {"#", "Operator Name", "Email", "Phone", "Assigned Classroom", "Status", "Last Login"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (ClassroomOperator op : operators) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(op.getOperatorName());
                row.createCell(2).setCellValue(op.getEmail());
                row.createCell(3).setCellValue(op.getPhone() != null ? op.getPhone() : "");
                row.createCell(4).setCellValue(op.getAssignedClassroom() != null ? op.getAssignedClassroom() : "");
                row.createCell(5).setCellValue(op.getStatus() != null ? op.getStatus().name() : UserStatus.ACTIVE.name());
                row.createCell(6).setCellValue(formatLastLogin(op.getLastLogin()));
            }

            // Auto-size all columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export for classroom operators", e);
        }
    }

    // -------------------------------------------------------------------------
    // Download Template (.xlsx) for Bulk Upload
    // -------------------------------------------------------------------------

    @Override
    public byte[] downloadTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Operators Template");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Operator Name", "Email", "Phone", "Assigned Classroom", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample rows to guide user
            Object[][] sampleData = {
                {"Deepak Kumar", "deepak.kumar@sunrise.edu.in", "9876543221", "Computer Lab 1", "Active"},
                {"Nisha Sharma", "nisha.sharma@sunrise.edu.in", "9876543222", "Math Lab", "Active"},
                {"Rohit Jaiswal", "rohit.jaiswal@sunrise.edu.in", "9876543223", "Science Lab 2", "Active"}
            };

            for (int r = 0; r < sampleData.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < sampleData[r].length; c++) {
                    row.createCell(c).setCellValue((String) sampleData[r][c]);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to create Excel template", e);
        }
    }

    // -------------------------------------------------------------------------
    // 2. Bulk Upload Classroom Operators from Excel (.xlsx / .xls)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ClassroomOperatorBulkUploadResponse bulkUploadClassroomOperators(Long schoolId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        School school = resolveSchool(schoolId);

        List<ClassroomOperator> createdList = new ArrayList<>();
        List<String> skippedEmails = new ArrayList<>();
        Set<String> seenEmailsInFile = new HashSet<>();
        int totalRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return ClassroomOperatorBulkUploadResponse.builder()
                        .totalProcessed(0)
                        .successCount(0)
                        .skippedCount(0)
                        .skippedEmails(Collections.emptyList())
                        .operators(Collections.emptyList())
                        .message("No data rows found in uploaded file")
                        .build();
            }

            // Detect column positions dynamically from header
            Row headerRow = sheet.getRow(0);
            int colName = -1, colEmail = -1, colPhone = -1, colClassroom = -1, colStatus = -1, colLastLogin = -1;

            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String h = getCellString(headerRow, c).toLowerCase();
                    if (h.contains("operator") || h.contains("name")) {
                        if (colName == -1) colName = c;
                    } else if (h.contains("email") || h.contains("mail")) {
                        if (colEmail == -1) colEmail = c;
                    } else if (h.contains("phone") || h.contains("mobile") || h.contains("contact")) {
                        if (colPhone == -1) colPhone = c;
                    } else if (h.contains("classroom") || h.contains("class") || h.contains("lab")) {
                        if (colClassroom == -1) colClassroom = c;
                    } else if (h.contains("status")) {
                        if (colStatus == -1) colStatus = c;
                    } else if (h.contains("login")) {
                        if (colLastLogin == -1) colLastLogin = c;
                    }
                }
            }

            // Fallbacks if header names didn't map
            if (colName == -1 || colEmail == -1) {
                // Check if column 0 is index "#"
                String firstHeader = headerRow != null ? getCellString(headerRow, 0) : "";
                boolean hasIndexCol = firstHeader.startsWith("#") || firstHeader.equalsIgnoreCase("id") || firstHeader.equalsIgnoreCase("sl");
                int offset = hasIndexCol ? 1 : 0;
                colName = offset;
                colEmail = offset + 1;
                colPhone = offset + 2;
                colClassroom = offset + 3;
                colStatus = offset + 4;
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String name = colName != -1 ? getCellString(row, colName) : "";
                String email = colEmail != -1 ? getCellString(row, colEmail) : "";
                String phone = colPhone != -1 ? getCellString(row, colPhone) : "";
                String classroom = colClassroom != -1 ? getCellString(row, colClassroom) : "";
                String statusStr = colStatus != -1 ? getCellString(row, colStatus) : "";

                if (name.isBlank() && email.isBlank()) {
                    continue; // skip blank row
                }

                totalRows++;
                String normalizedEmail = email.trim().toLowerCase();

                if (normalizedEmail.isBlank() || seenEmailsInFile.contains(normalizedEmail) || operatorRepository.existsByEmail(normalizedEmail)) {
                    skippedEmails.add(email.isBlank() ? ("Row " + (r + 1) + " (missing email)") : email);
                    continue;
                }

                seenEmailsInFile.add(normalizedEmail);
                UserStatus status = parseStatus(statusStr);

                ClassroomOperator op = ClassroomOperator.builder()
                        .operatorName(name.trim())
                        .email(normalizedEmail)
                        .phone(blankToNull(phone))
                        .assignedClassroom(blankToNull(classroom))
                        .status(status)
                        .school(school)
                        .build();

                createdList.add(operatorRepository.save(op));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse uploaded Excel file: " + e.getMessage(), e);
        }

        List<ClassroomOperatorResponse> responses = createdList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ClassroomOperatorBulkUploadResponse.builder()
                .totalProcessed(totalRows)
                .successCount(createdList.size())
                .skippedCount(skippedEmails.size())
                .skippedEmails(skippedEmails)
                .operators(responses)
                .message(String.format("Successfully uploaded %d operator(s). Skipped %d duplicate/invalid row(s).",
                        createdList.size(), skippedEmails.size()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private ClassroomOperatorResponse toResponse(ClassroomOperator op) {
        return ClassroomOperatorResponse.builder()
                .id(op.getId())
                .operatorName(op.getOperatorName())
                .name(op.getOperatorName())
                .initials(computeInitials(op.getOperatorName()))
                .email(op.getEmail())
                .phone(op.getPhone())
                .assignedClassroom(op.getAssignedClassroom())
                .status(op.getStatus())
                .lastLogin(op.getLastLogin())
                .lastLoginFormatted(formatLastLogin(op.getLastLogin()))
                .schoolId(op.getSchool() != null ? op.getSchool().getId() : null)
                .schoolName(op.getSchool() != null ? op.getSchool().getSchoolName() : null)
                .createdAt(op.getCreatedAt())
                .updatedAt(op.getUpdatedAt())
                .build();
    }

    private String computeInitials(String name) {
        if (name == null || name.isBlank()) return "";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String formatLastLogin(LocalDateTime dt) {
        if (dt == null) return "Never logged in";
        LocalDate today = LocalDate.now();
        LocalDate loginDate = dt.toLocalDate();

        if (loginDate.isEqual(today)) {
            return "Today, " + dt.format(TIME_FORMATTER);
        } else if (loginDate.isEqual(today.minusDays(1))) {
            return "Yesterday, " + dt.format(TIME_FORMATTER);
        } else {
            return dt.format(FULL_DATE_FORMATTER);
        }
    }

    private School resolveSchool(Long schoolId) {
        if (schoolId == null) {
            return null;
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

    private UserStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return UserStatus.ACTIVE;
        try {
            return UserStatus.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return s.trim().equalsIgnoreCase("inactive") ? UserStatus.INACTIVE : UserStatus.ACTIVE;
        }
    }
}

