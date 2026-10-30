package com.example.app.service.impl;

import com.example.app.dto.request.StudentCreateRequest;
import com.example.app.dto.request.StudentUpdateRequest;
import com.example.app.dto.response.StudentBulkUploadResponse;
import com.example.app.dto.response.StudentListResponse;
import com.example.app.dto.response.StudentResponse;
import com.example.app.entity.School;
import com.example.app.entity.Student;
import com.example.app.enums.Gender;
import com.example.app.enums.UserStatus;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.SchoolRepository;
import com.example.app.repository.StudentRepository;
import com.example.app.service.StudentService;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;

    private static final DateTimeFormatter DISPLAY_DOB_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private static final List<DateTimeFormatter> DOB_PARSE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    // -------------------------------------------------------------------------
    // 1. Create Student
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        String rollNumber = request.getRollNumber().trim();
        if (studentRepository.existsByRollNumber(rollNumber)) {
            throw new DuplicateResourceException("Student with roll number already exists: " + rollNumber);
        }

        School school = resolveSchool(request.getSchoolId());

        Student student = Student.builder()
                .rollNumber(rollNumber)
                .studentName(request.getStudentName() != null ? request.getStudentName().trim() : request.getName())
                .className(request.getClassName().trim())
                .section(request.getSection().trim().toUpperCase())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .parentName(blankToNull(request.getParentName()))
                .parentPhone(blankToNull(request.getParentPhone()))
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .school(school)
                .build();

        return toResponse(studentRepository.save(student));
    }

    // -------------------------------------------------------------------------
    // 2. Update Student
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (request.getRollNumber() != null && !request.getRollNumber().isBlank()) {
            String newRoll = request.getRollNumber().trim();
            if (!newRoll.equalsIgnoreCase(student.getRollNumber())) {
                if (studentRepository.existsByRollNumber(newRoll)) {
                    throw new DuplicateResourceException("Roll number already in use: " + newRoll);
                }
                student.setRollNumber(newRoll);
            }
        }

        if (request.getStudentName() != null && !request.getStudentName().isBlank()) {
            student.setStudentName(request.getStudentName().trim());
        } else if (request.getName() != null && !request.getName().isBlank()) {
            student.setStudentName(request.getName().trim());
        }

        if (request.getClassName() != null && !request.getClassName().isBlank()) {
            student.setClassName(request.getClassName().trim());
        }

        if (request.getSection() != null && !request.getSection().isBlank()) {
            student.setSection(request.getSection().trim().toUpperCase());
        }

        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }

        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getParentName() != null) {
            student.setParentName(blankToNull(request.getParentName()));
        }

        if (request.getParentPhone() != null) {
            student.setParentPhone(blankToNull(request.getParentPhone()));
        }

        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }

        if (request.getSchoolId() != null) {
            student.setSchool(resolveSchool(request.getSchoolId()));
        }

        return toResponse(studentRepository.save(student));
    }

    // -------------------------------------------------------------------------
    // 3. Get Single Student
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return toResponse(student);
    }

    // -------------------------------------------------------------------------
    // 4. Soft Delete Student (Sets status to INACTIVE)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        student.setStatus(UserStatus.INACTIVE);
        studentRepository.save(student);
    }

    // -------------------------------------------------------------------------
    // 5. View Students in Table with Filters & Counts
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public StudentListResponse listStudents(
            Long schoolId,
            String search,
            String className,
            String section,
            UserStatus status,
            Gender gender,
            Pageable pageable) {

        Page<Student> page = studentRepository.findWithFilters(
                schoolId,
                blankToNull(search),
                blankToNull(className),
                blankToNull(section),
                status,
                gender,
                pageable
        );

        List<StudentResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        long totalStudents = studentRepository.countTotal(schoolId);
        long activeStudents = studentRepository.countByStatus(schoolId, UserStatus.ACTIVE);
        long inactiveStudents = studentRepository.countByStatus(schoolId, UserStatus.INACTIVE);
        long maleStudents = studentRepository.countByGender(schoolId, Gender.MALE);
        long femaleStudents = studentRepository.countByGender(schoolId, Gender.FEMALE);

        return StudentListResponse.builder()
                .students(content)
                .content(content)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .inactiveStudents(inactiveStudents)
                .maleStudents(maleStudents)
                .femaleStudents(femaleStudents)
                .build();
    }

    // -------------------------------------------------------------------------
    // 6. Export Students to Excel (.xlsx)
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public byte[] exportStudentsToExcel(
            Long schoolId,
            String search,
            String className,
            String section,
            UserStatus status,
            Gender gender) {

        List<Student> students = studentRepository.findAllForExport(
                schoolId,
                blankToNull(search),
                blankToNull(className),
                blankToNull(section),
                status,
                gender
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Students");

            // Header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                "#", "Roll Number", "Student Name", "Class", "Section",
                "Gender", "Date of Birth", "Parent/Guardian", "Parent Phone", "Status"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (Student s : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(s.getRollNumber());
                row.createCell(2).setCellValue(s.getStudentName());
                row.createCell(3).setCellValue(s.getClassName());
                row.createCell(4).setCellValue(s.getSection());
                row.createCell(5).setCellValue(s.getGender() != null ? formatEnum(s.getGender().name()) : "");
                row.createCell(6).setCellValue(formatDateOfBirth(s.getDateOfBirth()));
                row.createCell(7).setCellValue(s.getParentName() != null ? s.getParentName() : "");
                row.createCell(8).setCellValue(s.getParentPhone() != null ? s.getParentPhone() : "");
                row.createCell(9).setCellValue(s.getStatus() != null ? s.getStatus().name() : UserStatus.ACTIVE.name());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export for students", e);
        }
    }

    // -------------------------------------------------------------------------
    // 7. Bulk Upload Students from Excel (.xlsx / .xls)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public StudentBulkUploadResponse bulkUploadStudents(Long schoolId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        School school = resolveSchool(schoolId);

        List<Student> createdList = new ArrayList<>();
        List<String> skippedRollNumbers = new ArrayList<>();
        Set<String> seenRollNumbers = new HashSet<>();
        int totalRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return StudentBulkUploadResponse.builder()
                        .totalProcessed(0)
                        .successCount(0)
                        .skippedCount(0)
                        .skippedRollNumbers(Collections.emptyList())
                        .students(Collections.emptyList())
                        .message("No data rows found in uploaded file")
                        .build();
            }

            // Dynamically match column indices from header row
            Row headerRow = sheet.getRow(0);
            int colRoll = -1, colName = -1, colClass = -1, colSection = -1, colGender = -1, colDob = -1, colParent = -1, colPhone = -1, colStatus = -1;

            if (headerRow != null) {
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    String h = getCellString(headerRow, c).toLowerCase();
                    if (h.contains("roll") && colRoll == -1) {
                        colRoll = c;
                    } else if ((h.contains("student") || h.contains("name")) && !h.contains("parent") && !h.contains("guardian") && colName == -1) {
                        colName = c;
                    } else if (h.contains("class") || h.contains("grade")) {
                        if (colClass == -1) colClass = c;
                    } else if (h.contains("section") || h.contains("sec")) {
                        if (colSection == -1) colSection = c;
                    } else if (h.contains("gender") || h.contains("sex")) {
                        if (colGender == -1) colGender = c;
                    } else if (h.contains("birth") || h.contains("dob")) {
                        if (colDob == -1) colDob = c;
                    } else if (h.contains("parent") || h.contains("guardian")) {
                        if (h.contains("phone") || h.contains("mobile") || h.contains("contact")) {
                            if (colPhone == -1) colPhone = c;
                        } else {
                            if (colParent == -1) colParent = c;
                        }
                    } else if ((h.contains("phone") || h.contains("mobile") || h.contains("contact")) && colPhone == -1) {
                        colPhone = c;
                    } else if (h.contains("status") && colStatus == -1) {
                        colStatus = c;
                    }
                }
            }

            // Fallback to standard index offsets
            if (colRoll == -1 || colName == -1) {
                String firstHeader = headerRow != null ? getCellString(headerRow, 0) : "";
                boolean hasIndexCol = firstHeader.startsWith("#") || firstHeader.equalsIgnoreCase("id") || firstHeader.equalsIgnoreCase("sl");
                int offset = hasIndexCol ? 1 : 0;
                colRoll = offset;
                colName = offset + 1;
                colClass = offset + 2;
                colSection = offset + 3;
                colGender = offset + 4;
                colDob = offset + 5;
                colParent = offset + 6;
                colPhone = offset + 7;
                colStatus = offset + 8;
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String roll = colRoll != -1 ? getCellString(row, colRoll) : "";
                String name = colName != -1 ? getCellString(row, colName) : "";
                String className = colClass != -1 ? getCellString(row, colClass) : "";
                String section = colSection != -1 ? getCellString(row, colSection) : "";
                String genderStr = colGender != -1 ? getCellString(row, colGender) : "";
                LocalDate dob = colDob != -1 ? parseDateOfBirth(row, colDob) : null;
                String parent = colParent != -1 ? getCellString(row, colParent) : "";
                String phone = colPhone != -1 ? getCellString(row, colPhone) : "";
                String statusStr = colStatus != -1 ? getCellString(row, colStatus) : "";

                if (roll.isBlank() && name.isBlank()) {
                    continue; // Skip blank line
                }

                totalRows++;
                String normalizedRoll = roll.trim();

                if (normalizedRoll.isBlank() || seenRollNumbers.contains(normalizedRoll) || studentRepository.existsByRollNumber(normalizedRoll)) {
                    skippedRollNumbers.add(normalizedRoll.isBlank() ? ("Row " + (r + 1) + " (missing roll number)") : normalizedRoll);
                    continue;
                }

                seenRollNumbers.add(normalizedRoll);
                Gender gender = parseGender(genderStr);
                UserStatus status = parseStatus(statusStr);

                Student student = Student.builder()
                        .rollNumber(normalizedRoll)
                        .studentName(name.trim())
                        .className(className.isBlank() ? "Class 8" : className.trim())
                        .section(section.isBlank() ? "A" : section.trim().toUpperCase())
                        .gender(gender)
                        .dateOfBirth(dob)
                        .parentName(blankToNull(parent))
                        .parentPhone(blankToNull(phone))
                        .status(status)
                        .school(school)
                        .build();

                createdList.add(studentRepository.save(student));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse uploaded Excel file: " + e.getMessage(), e);
        }

        List<StudentResponse> responses = createdList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return StudentBulkUploadResponse.builder()
                .totalProcessed(totalRows)
                .successCount(createdList.size())
                .skippedCount(skippedRollNumbers.size())
                .skippedRollNumbers(skippedRollNumbers)
                .students(responses)
                .message(String.format("Successfully uploaded %d student(s). Skipped %d duplicate/invalid row(s).",
                        createdList.size(), skippedRollNumbers.size()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private StudentResponse toResponse(Student s) {
        String parentDisplay = "";
        if (s.getParentName() != null && s.getParentPhone() != null) {
            parentDisplay = s.getParentName() + "\n" + s.getParentPhone();
        } else if (s.getParentName() != null) {
            parentDisplay = s.getParentName();
        } else if (s.getParentPhone() != null) {
            parentDisplay = s.getParentPhone();
        }

        return StudentResponse.builder()
                .id(s.getId())
                .rollNumber(s.getRollNumber())
                .studentName(s.getStudentName())
                .name(s.getStudentName())
                .initials(computeInitials(s.getStudentName()))
                .className(s.getClassName())
                .section(s.getSection())
                .gender(s.getGender())
                .dateOfBirth(s.getDateOfBirth())
                .dateOfBirthFormatted(formatDateOfBirth(s.getDateOfBirth()))
                .parentName(s.getParentName())
                .parentPhone(s.getParentPhone())
                .parentGuardianDisplay(parentDisplay)
                .status(s.getStatus())
                .schoolId(s.getSchool() != null ? s.getSchool().getId() : null)
                .schoolName(s.getSchool() != null ? s.getSchool().getSchoolName() : null)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
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

    private String formatDateOfBirth(LocalDate dob) {
        return dob != null ? dob.format(DISPLAY_DOB_FORMATTER) : "";
    }

    private LocalDate parseDateOfBirth(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        DataFormatter formatter = new DataFormatter();
        String str = formatter.formatCellValue(cell).trim();
        if (str.isBlank()) return null;

        for (DateTimeFormatter df : DOB_PARSE_FORMATTERS) {
            try {
                return LocalDate.parse(str, df);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private Gender parseGender(String g) {
        if (g == null || g.isBlank()) return null;
        String val = g.trim().toUpperCase();
        if (val.startsWith("M")) return Gender.MALE;
        if (val.startsWith("F")) return Gender.FEMALE;
        if (val.startsWith("O")) return Gender.OTHER;
        try {
            return Gender.valueOf(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private UserStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return UserStatus.ACTIVE;
        try {
            return UserStatus.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return s.trim().equalsIgnoreCase("inactive") ? UserStatus.INACTIVE : UserStatus.ACTIVE;
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

    private String formatEnum(String name) {
        if (name == null || name.isBlank()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}

