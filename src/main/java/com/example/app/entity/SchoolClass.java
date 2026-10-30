package com.example.app.entity;

import com.example.app.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a class (e.g., "Class 1", "Grade 1") belonging to a school.
 */
@Entity
@Table(
    name = "school_classes",
    indexes = {
        @Index(name = "idx_class_name",      columnList = "class_name"),
        @Index(name = "idx_class_school_id", columnList = "school_id"),
        @Index(name = "idx_class_status",    columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

    @Column(name = "grade_level", length = 100)
    private String gradeLevel;

    @Column(name = "total_sections")
    @Builder.Default
    private Integer totalSections = 0;

    @Column(name = "total_students")
    @Builder.Default
    private Integer totalStudents = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getName() {
        return className;
    }

    public void setName(String name) {
        this.className = name;
    }
}

