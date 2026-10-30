package com.example.app.repository;

import com.example.app.entity.SchoolClass;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    Optional<SchoolClass> findByClassNameAndSchoolId(String className, Long schoolId);

    @Query("""
        SELECT c FROM SchoolClass c
        WHERE (:schoolId IS NULL OR c.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(c.className)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c.gradeLevel) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:status IS NULL OR c.status = :status)
        ORDER BY c.id ASC
        """)
    Page<SchoolClass> findWithFilters(
        @Param("schoolId") Long schoolId,
        @Param("search")   String search,
        @Param("status")   UserStatus status,
        Pageable pageable
    );

    @Query("""
        SELECT c FROM SchoolClass c
        WHERE (:schoolId IS NULL OR c.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(c.className)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c.gradeLevel) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:status IS NULL OR c.status = :status)
        ORDER BY c.id ASC
        """)
    List<SchoolClass> findAllForExport(
        @Param("schoolId") Long schoolId,
        @Param("search")   String search,
        @Param("status")   UserStatus status
    );

    @Query("SELECT COUNT(c) FROM SchoolClass c WHERE (:schoolId IS NULL OR c.school.id = :schoolId)")
    long countTotal(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(c) FROM SchoolClass c WHERE (:schoolId IS NULL OR c.school.id = :schoolId) AND c.status = :status")
    long countByStatus(@Param("schoolId") Long schoolId, @Param("status") UserStatus status);

    @Query("SELECT COALESCE(SUM(c.totalSections), 0) FROM SchoolClass c WHERE (:schoolId IS NULL OR c.school.id = :schoolId)")
    long sumTotalSections(@Param("schoolId") Long schoolId);

    @Query("SELECT COALESCE(SUM(c.totalStudents), 0) FROM SchoolClass c WHERE (:schoolId IS NULL OR c.school.id = :schoolId)")
    long sumTotalStudents(@Param("schoolId") Long schoolId);
}

