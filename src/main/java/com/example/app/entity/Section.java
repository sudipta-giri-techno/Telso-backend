package com.example.app.entity;

import com.example.app.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a section (e.g. "Section A") under a class in a school.
 */
@Entity
@Table(
    name = "sections",
    indexes = {
        @Index(name = "idx_section_name",       columnList = "section_name"),
        @Index(name = "idx_section_class_num",   columnList = "class_numeric"),
        @Index(name = "idx_section_class_name",  columnList = "class_name"),
        @Index(name = "idx_section_school_id",   columnList = "school_id"),
        @Index(name = "idx_section_status",      columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_name", nullable = false, length = 100)
    private String sectionName;

    /**
     * e.g. "1", "2", "3"
     */
    @Column(name = "class_numeric", nullable = false, length = 50)
    private String classNumeric;

    /**
     * e.g. "Class 1", "Class 2"
     */
    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

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
        return sectionName;
    }

    public void setName(String name) {
        this.sectionName = name;
    }
}

