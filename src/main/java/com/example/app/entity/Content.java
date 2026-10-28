package com.example.app.entity;

import com.example.app.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entity representing educational content in Telso Saarthi Content Library.
 */
@Entity
@Table(
    name = "contents",
    indexes = {
        @Index(name = "idx_contents_type", columnList = "content_type"),
        @Index(name = "idx_contents_class", columnList = "target_class"),
        @Index(name = "idx_contents_subject", columnList = "subject"),
        @Index(name = "idx_contents_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 50)
    private ContentType contentType;

    @Column(name = "target_class", nullable = false, length = 50)
    private String targetClass;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(length = 150)
    private String chapter;

    @Column(length = 150)
    private String topic;

    @Column(length = 500)
    private String keywords;

    @Column(length = 500)
    private String notes;

    /**
     * Language of file: English and/or Manipuri (Monipuri).
     * User can select both or one.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "content_languages",
        joinColumns = @JoinColumn(name = "content_id")
    )
    @Column(name = "language", length = 30)
    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
