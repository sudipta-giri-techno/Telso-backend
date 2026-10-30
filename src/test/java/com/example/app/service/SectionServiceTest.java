package com.example.app.service;

import com.example.app.dto.request.SectionCreateRequest;
import com.example.app.dto.response.SectionListResponse;
import com.example.app.dto.response.SectionResponse;
import com.example.app.entity.School;
import com.example.app.entity.Section;
import com.example.app.enums.UserStatus;
import com.example.app.repository.SchoolRepository;
import com.example.app.repository.SectionRepository;
import com.example.app.service.impl.SectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SectionServiceImpl sectionService;

    private School mockSchool;
    private Section mockSection;

    @BeforeEach
    void setUp() {
        mockSchool = School.builder().id(1L).schoolName("Green Valley High School").build();
        mockSection = Section.builder()
                .id(1L)
                .sectionName("Section A")
                .classNumeric("1")
                .className("Class 1")
                .totalStudents(24)
                .status(UserStatus.ACTIVE)
                .school(mockSchool)
                .build();
    }

    @Test
    void testCreateSection_Success() {
        SectionCreateRequest request = SectionCreateRequest.builder()
                .sectionName("Section A")
                .classNumeric("1")
                .className("Class 1")
                .totalStudents(24)
                .schoolId(1L)
                .build();

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(mockSchool));
        when(sectionRepository.save(any(Section.class))).thenReturn(mockSection);

        SectionResponse response = sectionService.createSection(request);

        assertNotNull(response);
        assertEquals("Section A", response.getSectionName());
        assertEquals("1", response.getClassNumeric());
        assertEquals("Class 1", response.getClassName());
        assertEquals(24, response.getTotalStudents());
        assertEquals("A", response.getBadge());
    }

    @Test
    void testExportSectionsToExcel() {
        when(sectionRepository.findAllForExport(isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(mockSection));

        byte[] bytes = sectionService.exportSectionsToExcel(null, null, null, null);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void testListSections_ReturnsMetrics() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sectionRepository.findWithFilters(isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(mockSection), pageable, 12));
        when(sectionRepository.countTotal(isNull())).thenReturn(12L);
        when(sectionRepository.countByStatus(isNull(), eq(UserStatus.ACTIVE))).thenReturn(12L);
        when(sectionRepository.sumTotalStudents(isNull())).thenReturn(268L);

        SectionListResponse response = sectionService.listSections(null, null, null, null, pageable);

        assertNotNull(response);
        assertEquals(12L, response.getTotalSections());
        assertEquals(12L, response.getActiveSections());
        assertEquals(268L, response.getTotalStudents());
        assertEquals(22.3, response.getAverageStudents());
    }
}

