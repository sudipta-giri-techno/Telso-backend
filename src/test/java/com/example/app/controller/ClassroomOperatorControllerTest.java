package com.example.app.controller;

import com.example.app.dto.request.ClassroomOperatorCreateRequest;
import com.example.app.dto.request.ClassroomOperatorUpdateRequest;
import com.example.app.dto.response.ClassroomOperatorBulkUploadResponse;
import com.example.app.dto.response.ClassroomOperatorListResponse;
import com.example.app.dto.response.ClassroomOperatorResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.ClassroomOperatorService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClassroomOperatorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClassroomOperatorService classroomOperatorService;

    @InjectMocks
    private ClassroomOperatorController classroomOperatorController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(classroomOperatorController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateClassroomOperator_ReturnsCreated() throws Exception {
        ClassroomOperatorCreateRequest request = ClassroomOperatorCreateRequest.builder()
                .operatorName("Deepak Kumar")
                .email("deepak.kumar@sunrise.edu.in")
                .phone("9876543221")
                .assignedClassroom("Computer Lab 1")
                .status(UserStatus.ACTIVE)
                .build();

        ClassroomOperatorResponse response = ClassroomOperatorResponse.builder()
                .id(1L)
                .operatorName("Deepak Kumar")
                .email("deepak.kumar@sunrise.edu.in")
                .phone("9876543221")
                .assignedClassroom("Computer Lab 1")
                .status(UserStatus.ACTIVE)
                .build();

        when(classroomOperatorService.createClassroomOperator(any(ClassroomOperatorCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/classroom-operators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.operatorName").value("Deepak Kumar"))
                .andExpect(jsonPath("$.email").value("deepak.kumar@sunrise.edu.in"))
                .andExpect(jsonPath("$.assignedClassroom").value("Computer Lab 1"));
    }

    @Test
    void testUpdateClassroomOperator_ReturnsOk() throws Exception {
        ClassroomOperatorUpdateRequest request = ClassroomOperatorUpdateRequest.builder()
                .operatorName("Deepak Kumar Updated")
                .build();

        ClassroomOperatorResponse response = ClassroomOperatorResponse.builder()
                .id(1L)
                .operatorName("Deepak Kumar Updated")
                .email("deepak.kumar@sunrise.edu.in")
                .build();

        when(classroomOperatorService.updateClassroomOperator(eq(1L), any(ClassroomOperatorUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/classroom-operators/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operatorName").value("Deepak Kumar Updated"));
    }

    @Test
    void testGetClassroomOperator_ReturnsOk() throws Exception {
        ClassroomOperatorResponse response = ClassroomOperatorResponse.builder()
                .id(1L)
                .operatorName("Deepak Kumar")
                .email("deepak.kumar@sunrise.edu.in")
                .build();

        when(classroomOperatorService.getClassroomOperator(1L)).thenReturn(response);

        mockMvc.perform(get("/api/classroom-operators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.operatorName").value("Deepak Kumar"));
    }

    @Test
    void testDeleteClassroomOperator_ReturnsNoContent() throws Exception {
        doNothing().when(classroomOperatorService).deleteClassroomOperator(1L);

        mockMvc.perform(delete("/api/classroom-operators/1"))
                .andExpect(status().isNoContent());

        verify(classroomOperatorService).deleteClassroomOperator(1L);
    }

    @Test
    void testListClassroomOperators_ReturnsTableAndStatusMetrics() throws Exception {
        ClassroomOperatorResponse op = ClassroomOperatorResponse.builder()
                .id(1L)
                .operatorName("Deepak Kumar")
                .email("deepak.kumar@sunrise.edu.in")
                .assignedClassroom("Computer Lab 1")
                .status(UserStatus.ACTIVE)
                .build();

        ClassroomOperatorListResponse listResponse = ClassroomOperatorListResponse.builder()
                .operators(List.of(op))
                .content(List.of(op))
                .currentPage(0)
                .pageSize(10)
                .totalElements(28L)
                .totalPages(3)
                .totalOperators(28L)
                .activeOperators(24L)
                .inactiveOperators(4L)
                .recentlyAdded(4L)
                .assignedClassrooms(26L)
                .activePercentage(85.7)
                .inactivePercentage(14.3)
                .build();

        when(classroomOperatorService.listClassroomOperators(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/classroom-operators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOperators").value(28L))
                .andExpect(jsonPath("$.activeOperators").value(24L))
                .andExpect(jsonPath("$.inactiveOperators").value(4L))
                .andExpect(jsonPath("$.recentlyAdded").value(4L))
                .andExpect(jsonPath("$.assignedClassrooms").value(26L))
                .andExpect(jsonPath("$.activePercentage").value(85.7))
                .andExpect(jsonPath("$.inactivePercentage").value(14.3))
                .andExpect(jsonPath("$.operators[0].operatorName").value("Deepak Kumar"));
    }

    @Test
    void testExportClassroomOperators_ReturnsExcelAttachment() throws Exception {
        byte[] dummyExcel = new byte[]{1, 2, 3};
        when(classroomOperatorService.exportClassroomOperatorsToExcel(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(dummyExcel);

        mockMvc.perform(get("/api/classroom-operators/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"classroom_operators.xlsx\""))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(dummyExcel));
    }

    @Test
    void testDownloadTemplate_ReturnsExcelAttachment() throws Exception {
        byte[] dummyExcel = new byte[]{4, 5, 6};
        when(classroomOperatorService.downloadTemplate()).thenReturn(dummyExcel);

        mockMvc.perform(get("/api/classroom-operators/template"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"classroom_operators_template.xlsx\""))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(dummyExcel));
    }

    @Test
    void testBulkUpload_ReturnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "operators.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2}
        );

        ClassroomOperatorBulkUploadResponse response = ClassroomOperatorBulkUploadResponse.builder()
                .totalProcessed(1)
                .successCount(1)
                .skippedCount(0)
                .skippedEmails(Collections.emptyList())
                .operators(Collections.emptyList())
                .message("Successfully uploaded 1 operator(s).")
                .build();

        when(classroomOperatorService.bulkUploadClassroomOperators(isNull(), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/classroom-operators/bulk-upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.message").value("Successfully uploaded 1 operator(s)."));
    }
}

