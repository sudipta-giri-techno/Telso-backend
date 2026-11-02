package com.example.app.entity;

import com.example.app.enums.ContentInstallationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks the installation status of a content package for a specific classroom operator.
 */
@Entity
@Table(
    name = "content_installations",
    indexes = {
        @Index(name = "idx_ci_operator_id", columnList = "operator_id"),
        @Index(name = "idx_ci_content_id",  columnList = "content_id"),
        @Index(name = "idx_ci_status",       columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ci_operator_content", columnNames = {"operator_id", "content_id"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContentInstallation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private ClassroomOperator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContentInstallationStatus status = ContentInstallationStatus.NOT_INSTALLED;

    @Column(name = "installed_at")
    private LocalDateTime installedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
