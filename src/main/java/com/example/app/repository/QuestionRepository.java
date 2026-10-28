package com.example.app.repository;

import com.example.app.entity.Question;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuestionSetIdOrderByOrderIndexAscIdAsc(Long questionSetId);

    Optional<Question> findByIdAndQuestionSetId(Long id, Long questionSetId);

    long countByQuestionSetId(Long questionSetId);

    long countByQuestionSetIdAndIsApprovedTrue(Long questionSetId);

    @Query("""
        SELECT q FROM Question q
        WHERE q.questionSet.id = :questionSetId
        AND (:search IS NULL OR :search = ''
             OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(q.sourceReference) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:type IS NULL OR q.questionType = :type)
        AND (:difficulty IS NULL OR q.difficultyLevel = :difficulty)
        ORDER BY q.orderIndex ASC, q.id ASC
        """)
    Page<Question> findByQuestionSetIdWithFilters(
        @Param("questionSetId") Long questionSetId,
        @Param("search") String search,
        @Param("type") QuestionType type,
        @Param("difficulty") DifficultyLevel difficulty,
        Pageable pageable
    );
}

