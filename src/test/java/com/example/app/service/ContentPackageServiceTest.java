package com.example.app.service;

import com.example.app.dto.response.ContentPackageListResponse;
import com.example.app.dto.response.ContentPackageResponse;
import com.example.app.dto.response.ContentPackageStatsResponse;
import com.example.app.entity.ClassroomOperator;
import com.example.app.entity.Content;
import com.example.app.entity.ContentInstallation;
import com.example.app.enums.ContentInstallationStatus;
import com.example.app.enums.UserStatus;
import com.example.app.repository.ClassroomOperatorRepository;
import com.example.app.repository.ContentInstallationRepository;
import com.example.app.repository.ContentRepository;
import com.example.app.service.impl.ContentPackageServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentPackageServiceTest {

    @Mock private ContentRepository contentRepository;
    @Mock private ContentInstallationRepository installationRepository;
    @Mock private ClassroomOperatorRepository operatorRepository;

    @InjectMocks private ContentPackageServiceImpl service;

    private ClassroomOperator operator;
    private Content content;

    @BeforeEach
    void setUp() {
        operator = ClassroomOperator.builder()
                .id(1L)
                .operatorName("Test Operator")
                .status(UserStatus.ACTIVE)
                .build();

        content = Content.builder()
                .id(10L)
                .title("Science Package")
                .targetClass("8")
                .subject("Science")
                .active(true)
                .build();
    }

    @Test
    void listPackages_returnsResultsWithStats() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(contentRepository.findActiveContents(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(content)));
        when(installationRepository.findByOperatorId(1L)).thenReturn(Collections.emptyList());
        when(installationRepository.countByOperatorIdAndStatus(1L, ContentInstallationStatus.INSTALLED)).thenReturn(2L);
        when(installationRepository.countByOperatorIdAndStatus(1L, ContentInstallationStatus.DOWNLOADING)).thenReturn(1L);

        ContentPackageListResponse result = service.listPackages(
                1L, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getPackages()).hasSize(1);
        assertThat(result.getStats().getInstalled()).isEqualTo(2L);
        assertThat(result.getStats().getDownloading()).isEqualTo(1L);
    }

    @Test
    void listPackages_operatorNotFound_throwsException() {
        when(operatorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.listPackages(99L, null, null, null, null, null, PageRequest.of(0, 10)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPackage_returnsNotInstalledWhenNoRecord() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(installationRepository.findByOperatorIdAndContentId(1L, 10L)).thenReturn(Optional.empty());

        ContentPackageResponse result = service.getPackage(1L, 10L);

        assertThat(result.getInstallStatus()).isEqualTo(ContentInstallationStatus.NOT_INSTALLED);
        assertThat(result.getPackageName()).isEqualTo("Science Package");
    }

    @Test
    void installPackage_newInstall_setsDownloading() {
        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(installationRepository.findByOperatorIdAndContentId(1L, 10L)).thenReturn(Optional.empty());
        when(installationRepository.save(any())).thenAnswer(inv -> {
            ContentInstallation ci = inv.getArgument(0);
            ci.setId(100L);
            return ci;
        });

        ContentPackageResponse result = service.installPackage(1L, 10L);

        assertThat(result.getInstallStatus()).isEqualTo(ContentInstallationStatus.DOWNLOADING);
    }

    @Test
    void installPackage_downloading_setsInstalled() {
        ContentInstallation existing = ContentInstallation.builder()
                .id(100L)
                .operator(operator)
                .content(content)
                .status(ContentInstallationStatus.DOWNLOADING)
                .build();

        when(operatorRepository.findById(1L)).thenReturn(Optional.of(operator));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));
        when(installationRepository.findByOperatorIdAndContentId(1L, 10L)).thenReturn(Optional.of(existing));
        when(installationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContentPackageResponse result = service.installPackage(1L, 10L);

        assertThat(result.getInstallStatus()).isEqualTo(ContentInstallationStatus.INSTALLED);
    }

    @Test
    void getStats_returnsCorrectCounts() {
        when(contentRepository.countByActiveTrue()).thenReturn(28L);
        when(installationRepository.countByOperatorIdAndStatus(1L, ContentInstallationStatus.INSTALLED)).thenReturn(24L);
        when(installationRepository.countByOperatorIdAndStatus(1L, ContentInstallationStatus.DOWNLOADING)).thenReturn(2L);

        ContentPackageStatsResponse stats = service.getStats(1L);

        assertThat(stats.getTotalPackages()).isEqualTo(28L);
        assertThat(stats.getInstalled()).isEqualTo(24L);
        assertThat(stats.getDownloading()).isEqualTo(2L);
        assertThat(stats.getNotInstalled()).isEqualTo(2L);
    }
}

