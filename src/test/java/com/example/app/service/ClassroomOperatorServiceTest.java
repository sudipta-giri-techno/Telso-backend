package com.example.app.service;

import com.example.app.dto.request.ClassroomOperatorCreateRequest;
import com.example.app.dto.request.ClassroomOperatorUpdateRequest;
import com.example.app.dto.response.ClassroomOperatorBulkUploadResponse;
import com.example.app.dto.response.ClassroomOperatorListResponse;
import com.example.app.dto.response.ClassroomOperatorResponse;
import com.example.app.entity.ClassroomOperator;
import com.example.app.entity.School;
import com.example.app.enums.UserStatus;
import com.example.app.exception.DuplicateResourceException;
import com.example.app.repository.ClassroomOperatorRepository;
import com.example.app.repository.SchoolRepository;
import com.example.app.service.impl.ClassroomOperatorServiceImpl;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassroomOperatorServiceTest {

    @Mock
    private ClassroomOperatorRepository operatorRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private ClassroomOperatorServiceImpl operatorService;

    private School mockSchool;
    private ClassroomOperator mockOperator;

    @BeforeEach
    void setUp() {
        mockSchool = School.builder()
                .id(1L)
                .schoolName("Sunrise Public School")
                .schoolCode("SUN001")
                .build();

        mockOperator = ClassroomOperator.builder()
                .id(1L)
                .operatorName("Deepak Kumar")
                .email("deepak.kumar@sunrise.edu.in")
                .phone("9876543221")
                .assignedClassroom("Computer Lab 1")
                .status(UserStatus.ACTIVE)
                .lastLogin(LocalDateTime.now())
                .school(mockSchool)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateClassroomOperator_Success() {
        ClassroomOperatorCreateRequest request = ClassroomOperatorCreateRequest.builder()
                .operatorName("Deepak Kumar")
                .email("deepak.kumar@sunrise.edu.in")
                .phone("9876543221")
                .assignedClassroom("Computer Lab 1")
                .status(UserStatus.ACTIVE)
                .schoolId(1L)
                .build();

        when(operatorRepository.existsByEmail("deepak.kumar@sunrise.edu.in")).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(mockSchool));
        when(operatorRepository.save(any(ClassroomOperator.class))).thenReturn(mockOperator);

        ClassroomOperatorResponse response = operatorService.createClassroomOperator(request);

        assertNotNull(response);
        assertEquals("Deepak Kumar", response.getOperatorName());
        assertEquals("DK", response.getInitials());
        assertEquals("deepak.kumar@sunrise.edu.in", response.getEmail());
        assertEquals("Computer Lab 1", response.getAssignedClassroom());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        verify(operatorRepository).save(any(ClassroomOperator.class));
    }

    @Test
    void testCreateClassroomOperator_DuplicateEmail_ThrowsException() {
        ClassroomOperatorCreateRequest request = ClassroomOperatorCreateRequest.builder()
                .operatorName("Deepak Kumar")
                .email("deepak.kumar@sunrise.edu.in")
                .build();

        when(operatorRepository.existsByEmail("deepak.kumar@sunrise.edu.in")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> operatorService.createClassroomOperator(request));
        verify(operatorRepository, never()).save(any());
    }

    @Test
    void testUpdateClassroomOperator_Success() {
        ClassroomOperatorUpdateRequest request = ClassroomOperatorUpdateRequest.builder()
                .operatorName("Deepak K.")
                .assignedClassroom("Computer Lab 2")
                .status(UserStatus.INACTIVE)
                .build();

        when(operatorRepository.findById(1L)).thenReturn(Optional.of(mockOperator));
        when(operatorRepository.save(any(ClassroomOperator.class))).thenAnswer(i -> i.getArgument(0));

        ClassroomOperatorResponse response = operatorService.updateClassroomOperator(1L, request);

        assertEquals("Deepak K.", response.getOperatorName());
        assertEquals("Computer Lab 2", response.getAssignedClassroom());
        assertEquals(UserStatus.INACTIVE, response.getStatus());
    }

    @Test
    void testDeleteClassroomOperator_SoftDelete_SetsStatusToInactive() {
        assertEquals(UserStatus.ACTIVE, mockOperator.getStatus());

        when(operatorRepository.findById(1L)).thenReturn(Optional.of(mockOperator));
        when(operatorRepository.save(any(ClassroomOperator.class))).thenAnswer(i -> i.getArgument(0));

        operatorService.deleteClassroomOperator(1L);

        assertEquals(UserStatus.INACTIVE, mockOperator.getStatus());
        verify(operatorRepository).save(mockOperator);
        verify(operatorRepository, never()).delete(any());
    }

    @Test
    void testListClassroomOperators_ReturnsTableAndStatusMetrics() {
        Pageable pageable = PageRequest.of(0, 10);
        when(operatorRepository.findWithFilters(isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(mockOperator), pageable, 1));
        when(operatorRepository.countTotal(isNull())).thenReturn(28L);
        when(operatorRepository.countByStatus(isNull(), eq(UserStatus.ACTIVE))).thenReturn(24L);
        when(operatorRepository.countByStatus(isNull(), eq(UserStatus.INACTIVE))).thenReturn(4L);
        when(operatorRepository.countRecentlyAdded(isNull(), any(LocalDateTime.class))).thenReturn(4L);
        when(operatorRepository.countDistinctAssignedClassrooms(isNull())).thenReturn(26L);

        ClassroomOperatorListResponse response = operatorService.listClassroomOperators(null, null, null, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getOperators().size());
        assertEquals(28L, response.getTotalOperators());
        assertEquals(24L, response.getActiveOperators());
        assertEquals(4L, response.getInactiveOperators());
        assertEquals(4L, response.getRecentlyAdded());
        assertEquals(26L, response.getAssignedClassrooms());
        assertEquals(85.7, response.getActivePercentage());
        assertEquals(14.3, response.getInactivePercentage());
    }

    @Test
    void testExportClassroomOperatorsToExcel_GeneratesValidWorkbook() {
        when(operatorRepository.findAllForExport(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(mockOperator));

        byte[] excelBytes = operatorService.exportClassroomOperatorsToExcel(null, null, null, null);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    void testDownloadTemplate_GeneratesValidWorkbook() {
        byte[] templateBytes = operatorService.downloadTemplate();

        assertNotNull(templateBytes);
        assertTrue(templateBytes.length > 0);
    }

    @Test
    void testBulkUploadClassroomOperators_Success() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Operators");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Operator Name");
            header.createCell(1).setCellValue("Email");
            header.createCell(2).setCellValue("Phone");
            header.createCell(3).setCellValue("Assigned Classroom");
            header.createCell(4).setCellValue("Status");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Nisha Sharma");
            row1.createCell(1).setCellValue("nisha.sharma@sunrise.edu.in");
            row1.createCell(2).setCellValue("9876543222");
            row1.createCell(3).setCellValue("Math Lab");
            row1.createCell(4).setCellValue("Active");

            wb.write(out);
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "operators.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );

        when(operatorRepository.existsByEmail("nisha.sharma@sunrise.edu.in")).thenReturn(false);
        when(operatorRepository.save(any(ClassroomOperator.class))).thenAnswer(i -> {
            ClassroomOperator op = i.getArgument(0);
            op.setId(2L);
            return op;
        });

        ClassroomOperatorBulkUploadResponse response = operatorService.bulkUploadClassroomOperators(null, file);

        assertNotNull(response);
        assertEquals(1, response.getTotalProcessed());
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getSkippedCount());
        assertEquals(1, response.getOperators().size());
        assertEquals("Nisha Sharma", response.getOperators().get(0).getOperatorName());
    }
}

