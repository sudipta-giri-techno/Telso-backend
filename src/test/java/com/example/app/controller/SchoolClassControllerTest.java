package com.example.app.controller;

import com.example.app.dto.request.SchoolClassCreateRequest;
import com.example.app.dto.response.SchoolClassListResponse;
import com.example.app.dto.response.SchoolClassResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.SchoolClassService;
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
class SchoolClassControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SchoolClassService schoolClassService;

    @InjectMocks
    private SchoolClassController schoolClassController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(schoolClassController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateClass() throws Exception {
        SchoolClassCreateRequest request = SchoolClassCreateRequest.builder()
                .className("Class 1")
                .gradeLevel("Grade 1")
                .totalSections(2)
                .totalStudents(48)
                .build();

        SchoolClassResponse response = SchoolClassResponse.builder()
                .id(1L)
                .className("Class 1")
                .gradeLevel("Grade 1")
                .totalSections(2)
                .totalStudents(48)
                .status(UserStatus.ACTIVE)
                .build();

        when(schoolClassService.createClass(any(SchoolClassCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.className").value("Class 1"))
                .andExpect(jsonPath("$.gradeLevel").value("Grade 1"))
                .andExpect(jsonPath("$.totalSections").value(2))
                .andExpect(jsonPath("$.totalStudents").value(48));
    }

    @Test
    void testExportClasses() throws Exception {
        byte[] dummyExcel = new byte[]{1, 2, 3};
        when(schoolClassService.exportClassesToExcel(isNull(), isNull(), isNull())).thenReturn(dummyExcel);

        mockMvc.perform(get("/api/classes/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"classes.xlsx\""))
                .andExpect(content().bytes(dummyExcel));
    }

    @Test
    void testListClasses() throws Exception {
        SchoolClassResponse c = SchoolClassResponse.builder().id(1L).className("Class 1").build();
        SchoolClassListResponse listResponse = SchoolClassListResponse.builder()
                .classes(List.of(c))
                .totalClasses(6L)
                .totalSections(12L)
                .totalStudents(268L)
                .averageSections(2.0)
                .build();

        when(schoolClassService.listClasses(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClasses").value(6L))
                .andExpect(jsonPath("$.averageSections").value(2.0));
    }
}

