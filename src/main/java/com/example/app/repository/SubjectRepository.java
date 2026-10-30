package com.example.app.repository;

import com.example.app.entity.Subject;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("""
        SELECT DISTINCT s FROM Subject s
        LEFT JOIN s.classes c
        WHERE (:schoolId IS NULL OR s.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c)             LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:className IS NULL OR LOWER(c) = LOWER(CAST(:className AS string)))
        AND (:status    IS NULL OR s.status = :status)
        ORDER BY s.id ASC
        """)
    Page<Subject> findWithFilters(
        @Param("schoolId")  Long schoolId,
        @Param("search")    String search,
        @Param("className") String className,
        @Param("status")    UserStatus status,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT s FROM Subject s
        LEFT JOIN s.classes c
        WHERE (:schoolId IS NULL OR s.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c)             LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:className IS NULL OR LOWER(c) = LOWER(CAST(:className AS string)))
        AND (:status    IS NULL OR s.status = :status)
        ORDER BY s.id ASC
        """)
    List<Subject> findAllForExport(
        @Param("schoolId")  Long schoolId,
        @Param("search")    String search,
        @Param("className") String className,
        @Param("status")    UserStatus status
    );

    @Query("SELECT COUNT(s) FROM Subject s WHERE (:schoolId IS NULL OR s.school.id = :schoolId)")
    long countTotal(@Param("schoolId") Long schoolId);
}

