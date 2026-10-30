package com.example.app.repository;

import com.example.app.entity.Student;
import com.example.app.enums.Gender;
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
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByRollNumber(String rollNumber);

    Optional<Student> findByRollNumber(String rollNumber);

    /**
     * Filtered paginated query for the Students table view.
     */
    @Query("""
        SELECT s FROM Student s
        WHERE (:schoolId IS NULL OR s.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(s.rollNumber)   LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.studentName)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.className)    LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.section)      LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.parentName)   LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR s.parentPhone         LIKE CONCAT('%', CAST(:search AS string), '%'))
        AND (:className IS NULL OR LOWER(s.className) = LOWER(CAST(:className AS string)))
        AND (:section   IS NULL OR LOWER(s.section)   = LOWER(CAST(:section AS string)))
        AND (:status    IS NULL OR s.status = :status)
        AND (:gender    IS NULL OR s.gender = :gender)
        ORDER BY s.rollNumber ASC, s.id ASC
        """)
    Page<Student> findWithFilters(
        @Param("schoolId")  Long schoolId,
        @Param("search")    String search,
        @Param("className") String className,
        @Param("section")   String section,
        @Param("status")    UserStatus status,
        @Param("gender")    Gender gender,
        Pageable pageable
    );

    /**
     * Filtered query for Excel export without pagination.
     */
    @Query("""
        SELECT s FROM Student s
        WHERE (:schoolId IS NULL OR s.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(s.rollNumber)   LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.studentName)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.className)    LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.section)      LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(s.parentName)   LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR s.parentPhone         LIKE CONCAT('%', CAST(:search AS string), '%'))
        AND (:className IS NULL OR LOWER(s.className) = LOWER(CAST(:className AS string)))
        AND (:section   IS NULL OR LOWER(s.section)   = LOWER(CAST(:section AS string)))
        AND (:status    IS NULL OR s.status = :status)
        AND (:gender    IS NULL OR s.gender = :gender)
        ORDER BY s.rollNumber ASC, s.id ASC
        """)
    List<Student> findAllForExport(
        @Param("schoolId")  Long schoolId,
        @Param("search")    String search,
        @Param("className") String className,
        @Param("section")   String section,
        @Param("status")    UserStatus status,
        @Param("gender")    Gender gender
    );

    @Query("SELECT COUNT(s) FROM Student s WHERE (:schoolId IS NULL OR s.school.id = :schoolId)")
    long countTotal(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(s) FROM Student s WHERE (:schoolId IS NULL OR s.school.id = :schoolId) AND s.status = :status")
    long countByStatus(@Param("schoolId") Long schoolId, @Param("status") UserStatus status);

    @Query("SELECT COUNT(s) FROM Student s WHERE (:schoolId IS NULL OR s.school.id = :schoolId) AND s.gender = :gender")
    long countByGender(@Param("schoolId") Long schoolId, @Param("gender") Gender gender);
}

