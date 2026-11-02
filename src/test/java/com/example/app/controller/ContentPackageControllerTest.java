package com.example.app.controller;

import com.example.app.dto.request.InstallPackageRequest;
import com.example.app.dto.response.ContentPackageListResponse;
import com.example.app.dto.response.ContentPackageResponse;
import com.example.app.dto.response.ContentPackageStatsResponse;
import com.example.app.enums.ContentInstallationStatus;
import com.example.app.service.ContentPackageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContentPackageControllerTest {

    @Mock private ContentPackageService contentPackageService;
    @InjectMocks private ContentPackageController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    private ContentPackageStatsResponse sampleStats() {
        return ContentPackageStatsResponse.builder()
                .totalPackages(28).installed(24).downloading(2).notInstalled(2).build();
    }

    private ContentPackageResponse samplePackage() {
        return ContentPackageResponse.builder()
                .contentId(1L).packageName("Science").className("8")
                .subject("Science").installStatus(ContentInstallationStatus.INSTALLED)
                .build();
    }

    @Test
    void listPackages_returns200() throws Exception {
        ContentPackageListResponse response = ContentPackageListResponse.builder()
                .stats(sampleStats())
                .packages(List.of(samplePackage()))
                .page(0).size(10).totalElements(1).totalPages(1)
                .build();

        when(contentPackageService.listPackages(eq(1L), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/content-packages").param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.totalPackages").value(28))
                .andExpect(jsonPath("$.packages[0].packageName").value("Science"));
    }

    @Test
    void getPackage_returns200() throws Exception {
        when(contentPackageService.getPackage(1L, 10L)).thenReturn(samplePackage());

        mockMvc.perform(get("/api/content-packages/10").param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(1));
    }

    @Test
    void installPackage_returns200() throws Exception {
        ContentPackageResponse resp = ContentPackageResponse.builder()
                .contentId(10L).installStatus(ContentInstallationStatus.DOWNLOADING).build();

        when(contentPackageService.installPackage(1L, 10L)).thenReturn(resp);

        InstallPackageRequest req = new InstallPackageRequest();
        req.setOperatorId(1L);

        mockMvc.perform(post("/api/content-packages/10/install")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installStatus").value("DOWNLOADING"));
    }

    @Test
    void getStats_returns200() throws Exception {
        when(contentPackageService.getStats(1L)).thenReturn(sampleStats());

        mockMvc.perform(get("/api/content-packages/stats").param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPackages").value(28));
    }

    @Test
    void exportToExcel_returns200() throws Exception {
        when(contentPackageService.exportPackagesToExcel(1L)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/content-packages/export").param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"content-packages.xlsx\""));
    }
}

