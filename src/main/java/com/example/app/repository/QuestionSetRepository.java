package com.example.app.repository;

import com.example.app.entity.QuestionSet;
import com.example.app.enums.QuestionSetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionSetRepository extends JpaRepository<QuestionSet, Long> {

    long countByStatus(QuestionSetStatus status);

    boolean existsBySetId(String setId);

    Optional<QuestionSet> findBySetId(String setId);

    @Query("""
        SELECT qs FROM QuestionSet qs
        WHERE (:search IS NULL OR :search = ''
                OR LOWER(qs.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(qs.setId) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(qs.targetClass) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(qs.subject) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(qs.chapter) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(qs.topic) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:targetClass IS NULL OR :targetClass = '' OR LOWER(qs.targetClass) = LOWER(CAST(:targetClass AS string)))
        AND (:subject IS NULL OR :subject = '' OR LOWER(qs.subject) = LOWER(CAST(:subject AS string)))
        AND (:chapter IS NULL OR :chapter = '' OR LOWER(qs.chapter) = LOWER(CAST(:chapter AS string)))
        AND (:topic IS NULL OR :topic = '' OR LOWER(qs.topic) = LOWER(CAST(:topic AS string)))
        AND (:status IS NULL OR qs.status = :status)
        AND (:language IS NULL OR :language = '' OR :language MEMBER OF qs.languages)
        ORDER BY qs.createdAt DESC
        """)
    Page<QuestionSet> findWithFilters(
        @Param("search") String search,
        @Param("targetClass") String targetClass,
        @Param("subject") String subject,
        @Param("chapter") String chapter,
        @Param("topic") String topic,
        @Param("status") QuestionSetStatus status,
        @Param("language") String language,
        Pageable pageable
    );
}

