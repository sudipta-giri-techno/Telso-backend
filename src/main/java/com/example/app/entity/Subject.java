package com.example.app.entity;

import com.example.app.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Subject taught across multiple classes in a school.
 */
@Entity
@Table(
    name = "subjects",
    indexes = {
        @Index(name = "idx_subject_name",      columnList = "subject_name"),
        @Index(name = "idx_subject_school_id", columnList = "school_id"),
        @Index(name = "idx_subject_status",    columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_name", nullable = false, length = 150)
    private String subjectName;

    /**
     * Classes this subject is taught in, e.g. ["Class 6", "Class 7", "Class 8"].
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "subject_classes",
        joinColumns = @JoinColumn(name = "subject_id")
    )
    @Column(name = "class_name", length = 100)
    @Builder.Default
    private List<String> classes = new ArrayList<>();

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
        return subjectName;
    }

    public void setName(String name) {
        this.subjectName = name;
    }
}

