package com.example.app.entity;

import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing an individual Question within a Question Set.
 */
@Entity
@Table(
    name = "questions",
    indexes = {
        @Index(name = "idx_questions_set_id", columnList = "question_set_id"),
        @Index(name = "idx_questions_type", columnList = "question_type"),
        @Index(name = "idx_questions_difficulty", columnList = "difficulty_level"),
        @Index(name = "idx_questions_order", columnList = "question_set_id, order_index")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_set_id", nullable = false)
    @JsonIgnore
    private QuestionSet questionSet;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 1;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 30)
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    /**
     * Languages this question is available in (e.g., EN, MR).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "question_languages",
        joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "language", length = 30)
    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    /**
     * Options for Multiple Choice Questions.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "question_options",
        joinColumns = @JoinColumn(name = "question_id")
    )
    @OrderColumn(name = "option_index")
    @Column(name = "option_text", length = 500)
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

