package com.example.app.service;

import com.example.app.dto.request.SubjectCreateRequest;
import com.example.app.dto.response.SubjectListResponse;
import com.example.app.dto.response.SubjectResponse;
import com.example.app.entity.School;
import com.example.app.entity.Subject;
import com.example.app.enums.UserStatus;
import com.example.app.repository.SchoolRepository;
import com.example.app.repository.SubjectRepository;
import com.example.app.service.impl.SubjectServiceImpl;
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
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    private School mockSchool;
    private Subject mockSubject;

    @BeforeEach
    void setUp() {
        mockSchool = School.builder().id(1L).schoolName("Green Valley High School").build();
        mockSubject = Subject.builder()
                .id(1L)
                .subjectName("Physics")
                .classes(List.of("Class 6", "Class 7", "Class 8"))
                .status(UserStatus.ACTIVE)
                .school(mockSchool)
                .build();
    }

    @Test
    void testCreateSubject_Success() {
        SubjectCreateRequest request = SubjectCreateRequest.builder()
                .subjectName("Physics")
                .classes(List.of("Class 6", "Class 7", "Class 8"))
                .schoolId(1L)
                .build();

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(mockSchool));
        when(subjectRepository.save(any(Subject.class))).thenReturn(mockSubject);

        SubjectResponse response = subjectService.createSubject(request);

        assertNotNull(response);
        assertEquals("Physics", response.getSubjectName());
        assertEquals(3, response.getClasses().size());
        assertTrue(response.getClasses().contains("Class 6"));
    }

    @Test
    void testExportSubjectsToExcel() {
        when(subjectRepository.findAllForExport(isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(mockSubject));

        byte[] bytes = subjectService.exportSubjectsToExcel(null, null, null, null);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void testListSubjects_ReturnsCount() {
        Pageable pageable = PageRequest.of(0, 10);
        when(subjectRepository.findWithFilters(isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(mockSubject), pageable, 18));
        when(subjectRepository.countTotal(isNull())).thenReturn(18L);

        SubjectListResponse response = subjectService.listSubjects(null, null, null, null, pageable);

        assertNotNull(response);
        assertEquals(18L, response.getTotalSubjects());
        assertEquals(1, response.getSubjects().size());
    }
}

