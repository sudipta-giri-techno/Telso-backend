package com.example.app.repository;

import com.example.app.entity.Section;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    @Query("""
        SELECT s FROM Section s
        WHERE (:schoolId IS NULL OR s.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(s.sectionName)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.className)    LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.classNumeric) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:className IS NULL OR LOWER(s.className) = LOWER(CAST(:className AS string)) OR LOWER(s.classNumeric) = LOWER(CAST(:className AS string)))
        AND (:status    IS NULL OR s.status = :status)
        ORDER BY s.classNumeric ASC, s.sectionName ASC
        """)
    Page<Section> findWithFilters(
        @Param("schoolId")  Long schoolId,
        @Param("search")    String search,
        @Param("className") String className,
        @Param("status")    UserStatus status,
        Pageable pageable
    );

    @Query("""
        SELECT s FROM Section s
        WHERE (:schoolId IS NULL OR s.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(s.sectionName)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.className)    LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.classNumeric) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:className IS NULL OR LOWER(s.className) = LOWER(CAST(:className AS string)) OR LOWER(s.classNumeric) = LOWER(CAST(:className AS string)))
        AND (:status    IS NULL OR s.status = :status)
        ORDER BY s.classNumeric ASC, s.sectionName ASC
        """)
    List<Section> findAllForExport(
        @Param("schoolId")  Long schoolId,
        @Param("search")    String search,
        @Param("className") String className,
        @Param("status")    UserStatus status
    );

    @Query("SELECT COUNT(s) FROM Section s WHERE (:schoolId IS NULL OR s.school.id = :schoolId)")
    long countTotal(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(s) FROM Section s WHERE (:schoolId IS NULL OR s.school.id = :schoolId) AND s.status = :status")
    long countByStatus(@Param("schoolId") Long schoolId, @Param("status") UserStatus status);

    @Query("SELECT COALESCE(SUM(s.totalStudents), 0) FROM Section s WHERE (:schoolId IS NULL OR s.school.id = :schoolId)")
    long sumTotalStudents(@Param("schoolId") Long schoolId);
}

