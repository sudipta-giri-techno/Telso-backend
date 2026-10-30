package com.example.app.controller;

import com.example.app.dto.request.SubjectCreateRequest;
import com.example.app.dto.response.SubjectListResponse;
import com.example.app.dto.response.SubjectResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.SubjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubjectService subjectService;

    @InjectMocks
    private SubjectController subjectController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subjectController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateSubject() throws Exception {
        SubjectCreateRequest request = SubjectCreateRequest.builder()
                .subjectName("Physics")
                .classes(List.of("Class 6", "Class 7", "Class 8"))
                .build();

        SubjectResponse response = SubjectResponse.builder()
                .id(1L)
                .subjectName("Physics")
                .classes(List.of("Class 6", "Class 7", "Class 8"))
                .status(UserStatus.ACTIVE)
                .build();

        when(subjectService.createSubject(any(SubjectCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subjectName").value("Physics"))
                .andExpect(jsonPath("$.classes[0]").value("Class 6"));
    }

    @Test
    void testExportSubjects() throws Exception {
        byte[] dummyExcel = new byte[]{1, 2, 3};
        when(subjectService.exportSubjectsToExcel(isNull(), isNull(), isNull(), isNull())).thenReturn(dummyExcel);

        mockMvc.perform(get("/api/subjects/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"subjects.xlsx\""))
                .andExpect(content().bytes(dummyExcel));
    }

    @Test
    void testListSubjects() throws Exception {
        SubjectResponse s = SubjectResponse.builder().id(1L).subjectName("Physics").build();
        SubjectListResponse listResponse = SubjectListResponse.builder()
                .subjects(List.of(s))
                .totalSubjects(18L)
                .build();

        when(subjectService.listSubjects(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubjects").value(18L));
    }
}

