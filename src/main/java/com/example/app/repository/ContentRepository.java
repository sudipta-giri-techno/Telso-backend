package com.example.app.repository;

import com.example.app.entity.Content;
import com.example.app.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    long countByContentType(ContentType contentType);

    @Query("""
        SELECT c FROM Content c
        WHERE (:search IS NULL OR :search = ''
                OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c.subject) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c.chapter) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c.topic) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c.keywords) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:contentType IS NULL OR c.contentType = :contentType)
        AND (:targetClass IS NULL OR :targetClass = '' OR LOWER(c.targetClass) = LOWER(CAST(:targetClass AS string)))
        AND (:subject IS NULL OR :subject = '' OR LOWER(c.subject) = LOWER(CAST(:subject AS string)))
        AND (:language IS NULL OR :language = '' OR :language MEMBER OF c.languages)
        ORDER BY c.createdAt DESC
        """)
    Page<Content> findWithFilters(
        @Param("search") String search,
        @Param("contentType") ContentType contentType,
        @Param("targetClass") String targetClass,
        @Param("subject") String subject,
        @Param("language") String language,
        Pageable pageable
    );
}
