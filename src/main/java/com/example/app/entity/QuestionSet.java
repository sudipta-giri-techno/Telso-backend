package com.example.app.entity;

import com.example.app.enums.BloomsTaxonomy;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionSetStatus;
import com.example.app.enums.QuestionType;
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
 * Entity representing a Question Set in Telso Saarthi.
 */
@Entity
@Table(
    name = "question_sets",
    indexes = {
        @Index(name = "idx_qsets_set_id", columnList = "set_id", unique = true),
        @Index(name = "idx_qsets_status", columnList = "status"),
        @Index(name = "idx_qsets_class", columnList = "target_class"),
        @Index(name = "idx_qsets_subject", columnList = "subject"),
        @Index(name = "idx_qsets_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique display code/ID such as "QS-0001".
     */
    @Column(name = "set_id", nullable = false, unique = true, length = 50)
    private String setId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "target_class", nullable = false, length = 50)
    private String targetClass;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, length = 150)
    private String chapter;

    @Column(length = 150)
    private String topic;

    /**
     * Languages supported (e.g. EN, MR).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "question_set_languages",
        joinColumns = @JoinColumn(name = "question_set_id")
    )
    @Column(name = "language", length = 30)
    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    /**
     * Configured number of questions (e.g., 25).
     */
    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    /**
     * Configured question types for this set (MCQ, True/False, Short Answer, Long Answer, etc.).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "question_set_types",
        joinColumns = @JoinColumn(name = "question_set_id")
    )
    @Column(name = "question_type", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<QuestionType> questionTypes = new LinkedHashSet<>();

    /**
     * Difficulty levels configured (Easy, Medium, Hard).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "question_set_difficulties",
        joinColumns = @JoinColumn(name = "question_set_id")
    )
    @Column(name = "difficulty_level", length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DifficultyLevel> difficultyLevels = new LinkedHashSet<>();

    /**
     * Bloom's Taxonomy cognitive dimensions (Remember, Understand, Apply, etc.).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "question_set_blooms",
        joinColumns = @JoinColumn(name = "question_set_id")
    )
    @Column(name = "blooms_taxonomy", length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<BloomsTaxonomy> bloomsTaxonomy = new LinkedHashSet<>();

    @Column(name = "include_explanations", nullable = false)
    @Builder.Default
    private Boolean includeExplanations = false;

    @Column(name = "include_source_reference", nullable = false)
    @Builder.Default
    private Boolean includeSourceReference = false;

    @Column(name = "randomize_options", nullable = false)
    @Builder.Default
    private Boolean randomizeOptions = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private QuestionSetStatus status = QuestionSetStatus.DRAFT;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "question_set_tags",
        joinColumns = @JoinColumn(name = "question_set_id")
    )
    @Column(name = "tag", length = 50)
    @Builder.Default
    private Set<String> tags = new LinkedHashSet<>();

    @Column(name = "generated_by", length = 100)
    @Builder.Default
    private String generatedBy = "AI";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC, id ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addQuestion(Question question) {
        if (question == null) return;
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }
        if (question.getOrderIndex() == null || question.getOrderIndex() <= 0) {
            question.setOrderIndex(this.questions.size() + 1);
        }
        question.setQuestionSet(this);
        this.questions.add(question);
    }

    public void removeQuestion(Question question) {
        if (question == null || this.questions == null) return;
        this.questions.remove(question);
        question.setQuestionSet(null);
    }

    public int getTotalQuestionsCount() {
        return this.questions != null ? this.questions.size() : 0;
    }

    public int getApprovedQuestionsCount() {
        if (this.questions == null) return 0;
        return (int) this.questions.stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsApproved()))
                .count();
    }
}

