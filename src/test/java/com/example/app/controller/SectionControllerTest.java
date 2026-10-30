package com.example.app.controller;

import com.example.app.dto.request.SectionCreateRequest;
import com.example.app.dto.response.SectionListResponse;
import com.example.app.dto.response.SectionResponse;
import com.example.app.enums.UserStatus;
import com.example.app.service.SectionService;
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
class SectionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private SectionController sectionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sectionController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateSection() throws Exception {
        SectionCreateRequest request = SectionCreateRequest.builder()
                .sectionName("Section A")
                .classNumeric("1")
                .className("Class 1")
                .totalStudents(24)
                .build();

        SectionResponse response = SectionResponse.builder()
                .id(1L)
                .sectionName("Section A")
                .classNumeric("1")
                .className("Class 1")
                .totalStudents(24)
                .status(UserStatus.ACTIVE)
                .build();

        when(sectionService.createSection(any(SectionCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sectionName").value("Section A"))
                .andExpect(jsonPath("$.classNumeric").value("1"))
                .andExpect(jsonPath("$.className").value("Class 1"))
                .andExpect(jsonPath("$.totalStudents").value(24));
    }

    @Test
    void testExportSections() throws Exception {
        byte[] dummyExcel = new byte[]{1, 2, 3};
        when(sectionService.exportSectionsToExcel(isNull(), isNull(), isNull(), isNull())).thenReturn(dummyExcel);

        mockMvc.perform(get("/api/sections/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"sections.xlsx\""))
                .andExpect(content().bytes(dummyExcel));
    }

    @Test
    void testListSections() throws Exception {
        SectionResponse s = SectionResponse.builder().id(1L).sectionName("Section A").build();
        SectionListResponse listResponse = SectionListResponse.builder()
                .sections(List.of(s))
                .totalSections(12L)
                .activeSections(12L)
                .totalStudents(268L)
                .averageStudents(22.3)
                .build();

        when(sectionService.listSections(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSections").value(12L))
                .andExpect(jsonPath("$.averageStudents").value(22.3));
    }
}

