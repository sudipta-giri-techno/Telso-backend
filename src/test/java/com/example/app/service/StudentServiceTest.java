package com.example.app.service;

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
import com.example.app.repository.SchoolRepository;
import com.example.app.repository.StudentRepository;
import com.example.app.service.impl.StudentServiceImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private School mockSchool;
    private Student mockStudent;

    @BeforeEach
    void setUp() {
        mockSchool = School.builder()
                .id(1L)
                .schoolName("Sunrise Public School")
                .schoolCode("SUN001")
                .build();

        mockStudent = Student.builder()
                .id(1L)
                .rollNumber("SHS/23/0001")
                .studentName("Ananya Rani")
                .className("Class 8")
                .section("A")
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(2011, 2, 12))
                .parentName("Rajesh Rani")
                .parentPhone("9876543210")
                .status(UserStatus.ACTIVE)
                .school(mockSchool)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateStudent_Success() {
        StudentCreateRequest request = StudentCreateRequest.builder()
                .rollNumber("SHS/23/0001")
                .studentName("Ananya Rani")
                .className("Class 8")
                .section("A")
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(2011, 2, 12))
                .parentName("Rajesh Rani")
                .parentPhone("9876543210")
                .status(UserStatus.ACTIVE)
                .schoolId(1L)
                .build();

        when(studentRepository.existsByRollNumber("SHS/23/0001")).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(mockSchool));
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);

        StudentResponse response = studentService.createStudent(request);

        assertNotNull(response);
        assertEquals("SHS/23/0001", response.getRollNumber());
        assertEquals("Ananya Rani", response.getStudentName());
        assertEquals("AR", response.getInitials());
        assertEquals("Class 8", response.getClassName());
        assertEquals("A", response.getSection());
        assertEquals(Gender.FEMALE, response.getGender());
        assertEquals("12 Feb 2011", response.getDateOfBirthFormatted());
        assertEquals("Rajesh Rani", response.getParentName());
        assertEquals("9876543210", response.getParentPhone());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void testCreateStudent_DuplicateRollNumber_ThrowsException() {
        StudentCreateRequest request = StudentCreateRequest.builder()
                .rollNumber("SHS/23/0001")
                .studentName("Ananya Rani")
                .build();

        when(studentRepository.existsByRollNumber("SHS/23/0001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(request));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void testUpdateStudent_Success() {
        StudentUpdateRequest request = StudentUpdateRequest.builder()
                .studentName("Ananya R.")
                .section("B")
                .status(UserStatus.INACTIVE)
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        StudentResponse response = studentService.updateStudent(1L, request);

        assertEquals("Ananya R.", response.getStudentName());
        assertEquals("B", response.getSection());
        assertEquals(UserStatus.INACTIVE, response.getStatus());
    }

    @Test
    void testDeleteStudent_SoftDelete_SetsStatusToInactive() {
        assertEquals(UserStatus.ACTIVE, mockStudent.getStatus());

        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        studentService.deleteStudent(1L);

        assertEquals(UserStatus.INACTIVE, mockStudent.getStatus());
        verify(studentRepository).save(mockStudent);
        verify(studentRepository, never()).delete(any());
    }

    @Test
    void testListStudents_ReturnsPaginatedWithCounts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(studentRepository.findWithFilters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(mockStudent), pageable, 1256));
        when(studentRepository.countTotal(isNull())).thenReturn(1256L);
        when(studentRepository.countByStatus(isNull(), eq(UserStatus.ACTIVE))).thenReturn(1200L);
        when(studentRepository.countByStatus(isNull(), eq(UserStatus.INACTIVE))).thenReturn(56L);
        when(studentRepository.countByGender(isNull(), eq(Gender.MALE))).thenReturn(650L);
        when(studentRepository.countByGender(isNull(), eq(Gender.FEMALE))).thenReturn(606L);

        StudentListResponse response = studentService.listStudents(null, null, null, null, null, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getStudents().size());
        assertEquals(1256L, response.getTotalStudents());
        assertEquals(1200L, response.getActiveStudents());
        assertEquals(56L, response.getInactiveStudents());
        assertEquals(650L, response.getMaleStudents());
        assertEquals(606L, response.getFemaleStudents());
    }

    @Test
    void testExportStudentsToExcel_GeneratesValidWorkbook() {
        when(studentRepository.findAllForExport(isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(mockStudent));

        byte[] excelBytes = studentService.exportStudentsToExcel(null, null, null, null, null, null);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    void testBulkUploadStudents_Success() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Roll Number");
            header.createCell(1).setCellValue("Student Name");
            header.createCell(2).setCellValue("Class");
            header.createCell(3).setCellValue("Section");
            header.createCell(4).setCellValue("Gender");
            header.createCell(5).setCellValue("Date of Birth");
            header.createCell(6).setCellValue("Parent/Guardian");
            header.createCell(7).setCellValue("Parent Phone");
            header.createCell(8).setCellValue("Status");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("SHS/23/0002");
            row1.createCell(1).setCellValue("Vikash Kumar");
            row1.createCell(2).setCellValue("Class 8");
            row1.createCell(3).setCellValue("A");
            row1.createCell(4).setCellValue("Male");
            row1.createCell(5).setCellValue("05 Jul 2010");
            row1.createCell(6).setCellValue("Suresh Kumar");
            row1.createCell(7).setCellValue("9876543211");
            row1.createCell(8).setCellValue("Active");

            wb.write(out);
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );

        when(studentRepository.existsByRollNumber("SHS/23/0002")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> {
            Student s = i.getArgument(0);
            s.setId(2L);
            return s;
        });

        StudentBulkUploadResponse response = studentService.bulkUploadStudents(null, file);

        assertNotNull(response);
        assertEquals(1, response.getTotalProcessed());
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getSkippedCount());
        assertEquals(1, response.getStudents().size());
        assertEquals("Vikash Kumar", response.getStudents().get(0).getStudentName());
        assertEquals("VK", response.getStudents().get(0).getInitials());
    }
}

