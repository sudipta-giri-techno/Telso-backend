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
 * Represents a teacher belonging to a school.
 * Each teacher can be assigned to multiple class-sections (e.g., "7B", "8A").
 */
@Entity
@Table(
    name = "teachers",
    indexes = {
        @Index(name = "idx_teacher_email",    columnList = "email",     unique = true),
        @Index(name = "idx_teacher_school_id", columnList = "school_id"),
        @Index(name = "idx_teacher_status",   columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    /** Subject the teacher specialises in, e.g. "Mathematics". */
    @Column(name = "subject", length = 150)
    private String subject;

    /**
     * Assigned class-sections stored as a collection of strings like "7B", "8A", "9C".
     * The class number and section letter are kept together for easy display.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "teacher_class_sections",
        joinColumns = @JoinColumn(name = "teacher_id")
    )
    @Column(name = "class_section", length = 20)
    @Builder.Default
    private List<String> classSections = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /** The school this teacher belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
