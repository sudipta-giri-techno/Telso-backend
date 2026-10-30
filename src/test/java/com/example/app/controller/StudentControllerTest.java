package com.example.app.controller;

import com.example.app.dto.request.StudentCreateRequest;
import com.example.app.dto.request.StudentUpdateRequest;
import com.example.app.dto.response.StudentBulkUploadResponse;
import com.example.app.dto.response.StudentListResponse;
import com.example.app.dto.response.StudentResponse;
import com.example.app.enums.Gender;
import com.example.app.enums.UserStatus;
import com.example.app.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreateStudent_ReturnsCreated() throws Exception {
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
                .build();

        StudentResponse response = StudentResponse.builder()
                .id(1L)
                .rollNumber("SHS/23/0001")
                .studentName("Ananya Rani")
                .initials("AR")
                .className("Class 8")
                .section("A")
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(2011, 2, 12))
                .dateOfBirthFormatted("12 Feb 2011")
                .parentName("Rajesh Rani")
                .parentPhone("9876543210")
                .status(UserStatus.ACTIVE)
                .build();

        when(studentService.createStudent(any(StudentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rollNumber").value("SHS/23/0001"))
                .andExpect(jsonPath("$.studentName").value("Ananya Rani"))
                .andExpect(jsonPath("$.initials").value("AR"))
                .andExpect(jsonPath("$.className").value("Class 8"))
                .andExpect(jsonPath("$.dateOfBirthFormatted").value("12 Feb 2011"));
    }

    @Test
    void testUpdateStudent_ReturnsOk() throws Exception {
        StudentUpdateRequest request = StudentUpdateRequest.builder()
                .studentName("Ananya Rani Updated")
                .build();

        StudentResponse response = StudentResponse.builder()
                .id(1L)
                .rollNumber("SHS/23/0001")
                .studentName("Ananya Rani Updated")
                .build();

        when(studentService.updateStudent(eq(1L), any(StudentUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Ananya Rani Updated"));
    }

    @Test
    void testGetStudent_ReturnsOk() throws Exception {
        StudentResponse response = StudentResponse.builder()
                .id(1L)
                .rollNumber("SHS/23/0001")
                .studentName("Ananya Rani")
                .build();

        when(studentService.getStudent(1L)).thenReturn(response);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rollNumber").value("SHS/23/0001"));
    }

    @Test
    void testDeleteStudent_ReturnsNoContent() throws Exception {
        doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());

        verify(studentService).deleteStudent(1L);
    }

    @Test
    void testListStudents_ReturnsPaginatedAndCounts() throws Exception {
        StudentResponse student = StudentResponse.builder()
                .id(1L)
                .rollNumber("SHS/23/0001")
                .studentName("Ananya Rani")
                .className("Class 8")
                .section("A")
                .gender(Gender.FEMALE)
                .dateOfBirthFormatted("12 Feb 2011")
                .status(UserStatus.ACTIVE)
                .build();

        StudentListResponse listResponse = StudentListResponse.builder()
                .students(List.of(student))
                .content(List.of(student))
                .currentPage(0)
                .pageSize(10)
                .totalElements(1256L)
                .totalPages(126)
                .totalStudents(1256L)
                .activeStudents(1200L)
                .inactiveStudents(56L)
                .maleStudents(650L)
                .femaleStudents(606L)
                .build();

        when(studentService.listStudents(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(1256L))
                .andExpect(jsonPath("$.activeStudents").value(1200L))
                .andExpect(jsonPath("$.inactiveStudents").value(56L))
                .andExpect(jsonPath("$.students[0].rollNumber").value("SHS/23/0001"));
    }

    @Test
    void testExportStudents_ReturnsExcelAttachment() throws Exception {
        byte[] dummyExcel = new byte[]{1, 2, 3};
        when(studentService.exportStudentsToExcel(isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(dummyExcel);

        mockMvc.perform(get("/api/students/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"students.xlsx\""))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(dummyExcel));
    }

    @Test
    void testBulkUpload_ReturnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2}
        );

        StudentBulkUploadResponse response = StudentBulkUploadResponse.builder()
                .totalProcessed(1)
                .successCount(1)
                .skippedCount(0)
                .skippedRollNumbers(Collections.emptyList())
                .students(Collections.emptyList())
                .message("Successfully uploaded 1 student(s).")
                .build();

        when(studentService.bulkUploadStudents(isNull(), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/students/bulk-upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.message").value("Successfully uploaded 1 student(s)."));
    }
}

