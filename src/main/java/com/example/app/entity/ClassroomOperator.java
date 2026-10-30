package com.example.app.entity;

import com.example.app.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a classroom operator assigned to a school and a specific classroom.
 */
@Entity
@Table(
    name = "classroom_operators",
    indexes = {
        @Index(name = "idx_operator_email",       columnList = "email", unique = true),
        @Index(name = "idx_operator_school_id",   columnList = "school_id"),
        @Index(name = "idx_operator_status",      columnList = "status"),
        @Index(name = "idx_operator_classroom",   columnList = "assigned_classroom")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomOperator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_name", nullable = false, length = 150)
    private String operatorName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Classroom or lab assigned to this operator, e.g. "Computer Lab 1", "Math Lab".
     */
    @Column(name = "assigned_classroom", length = 150)
    private String assignedClassroom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /** The school this classroom operator belongs to. */
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
        return operatorName;
    }

    public void setName(String name) {
        this.operatorName = name;
    }
}

