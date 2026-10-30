package com.example.app.entity;

import com.example.app.enums.Gender;
import com.example.app.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a student enrolled in a school.
 */
@Entity
@Table(
    name = "students",
    indexes = {
        @Index(name = "idx_student_roll_number", columnList = "roll_number", unique = true),
        @Index(name = "idx_student_school_id",   columnList = "school_id"),
        @Index(name = "idx_student_class",       columnList = "class_name"),
        @Index(name = "idx_student_section",     columnList = "section"),
        @Index(name = "idx_student_status",      columnList = "status"),
        @Index(name = "idx_student_gender",      columnList = "gender")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roll_number", nullable = false, unique = true, length = 50)
    private String rollNumber;

    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "section", nullable = false, length = 20)
    private String section;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "parent_name", length = 150)
    private String parentName;

    @Column(name = "parent_phone", length = 20)
    private String parentPhone;

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
        return studentName;
    }

    public void setName(String name) {
        this.studentName = name;
    }
}

