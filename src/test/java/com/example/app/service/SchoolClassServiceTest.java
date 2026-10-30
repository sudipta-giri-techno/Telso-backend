package com.example.app.service;

import com.example.app.dto.request.SchoolClassCreateRequest;
import com.example.app.dto.request.SchoolClassUpdateRequest;
import com.example.app.dto.response.SchoolClassListResponse;
import com.example.app.dto.response.SchoolClassResponse;
import com.example.app.entity.School;
import com.example.app.entity.SchoolClass;
import com.example.app.enums.UserStatus;
import com.example.app.repository.SchoolClassRepository;
import com.example.app.repository.SchoolRepository;
import com.example.app.service.impl.SchoolClassServiceImpl;
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
class SchoolClassServiceTest {

    @Mock
    private SchoolClassRepository classRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolClassServiceImpl classService;

    private School mockSchool;
    private SchoolClass mockClass;

    @BeforeEach
    void setUp() {
        mockSchool = School.builder().id(1L).schoolName("Green Valley High School").build();
        mockClass = SchoolClass.builder()
                .id(1L)
                .className("Class 1")
                .gradeLevel("Grade 1")
                .totalSections(2)
                .totalStudents(48)
                .status(UserStatus.ACTIVE)
                .school(mockSchool)
                .build();
    }

    @Test
    void testCreateClass_Success() {
        SchoolClassCreateRequest request = SchoolClassCreateRequest.builder()
                .className("Class 1")
                .gradeLevel("Grade 1")
                .totalSections(2)
                .totalStudents(48)
                .schoolId(1L)
                .build();

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(mockSchool));
        when(classRepository.save(any(SchoolClass.class))).thenReturn(mockClass);

        SchoolClassResponse response = classService.createClass(request);

        assertNotNull(response);
        assertEquals("Class 1", response.getClassName());
        assertEquals("Grade 1", response.getGradeLevel());
        assertEquals(2, response.getTotalSections());
        assertEquals(48, response.getTotalStudents());
    }

    @Test
    void testExportClassesToExcel() {
        when(classRepository.findAllForExport(isNull(), isNull(), isNull())).thenReturn(List.of(mockClass));

        byte[] bytes = classService.exportClassesToExcel(null, null, null);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void testListClasses_ReturnsMetrics() {
        Pageable pageable = PageRequest.of(0, 10);
        when(classRepository.findWithFilters(isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(mockClass), pageable, 6));
        when(classRepository.countTotal(isNull())).thenReturn(6L);
        when(classRepository.sumTotalSections(isNull())).thenReturn(12L);
        when(classRepository.sumTotalStudents(isNull())).thenReturn(268L);

        SchoolClassListResponse response = classService.listClasses(null, null, null, pageable);

        assertNotNull(response);
        assertEquals(6L, response.getTotalClasses());
        assertEquals(12L, response.getTotalSections());
        assertEquals(268L, response.getTotalStudents());
        assertEquals(2.0, response.getAverageSections());
    }
}

