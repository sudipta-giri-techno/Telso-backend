package com.example.app.repository;

import com.example.app.entity.ClassroomOperator;
import com.example.app.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomOperatorRepository extends JpaRepository<ClassroomOperator, Long> {

    boolean existsByEmail(String email);

    Optional<ClassroomOperator> findByEmail(String email);

    /**
     * Filtered paginated query for Classroom Operators table view.
     */
    @Query("""
        SELECT c FROM ClassroomOperator c
        WHERE (:schoolId IS NULL OR c.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(c.operatorName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c.email)        LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR c.phone               LIKE CONCAT('%', CAST(:search AS string), '%')
                OR LOWER(c.assignedClassroom) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:status IS NULL OR c.status = :status)
        AND (:assignedClassroom IS NULL OR LOWER(c.assignedClassroom) = LOWER(CAST(:assignedClassroom AS string)))
        ORDER BY c.createdAt DESC
        """)
    Page<ClassroomOperator> findWithFilters(
        @Param("schoolId") Long schoolId,
        @Param("search") String search,
        @Param("status") UserStatus status,
        @Param("assignedClassroom") String assignedClassroom,
        Pageable pageable
    );

    /**
     * Filtered query for Excel export without pagination.
     */
    @Query("""
        SELECT c FROM ClassroomOperator c
        WHERE (:schoolId IS NULL OR c.school.id = :schoolId)
        AND (:search IS NULL
                OR LOWER(c.operatorName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(c.email)        LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR c.phone               LIKE CONCAT('%', CAST(:search AS string), '%')
                OR LOWER(c.assignedClassroom) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:status IS NULL OR c.status = :status)
        AND (:assignedClassroom IS NULL OR LOWER(c.assignedClassroom) = LOWER(CAST(:assignedClassroom AS string)))
        ORDER BY c.createdAt DESC
        """)
    List<ClassroomOperator> findAllForExport(
        @Param("schoolId") Long schoolId,
        @Param("search") String search,
        @Param("status") UserStatus status,
        @Param("assignedClassroom") String assignedClassroom
    );

    /**
     * Total operator count for given school (or entire system if schoolId is null).
     */
    @Query("SELECT COUNT(c) FROM ClassroomOperator c WHERE (:schoolId IS NULL OR c.school.id = :schoolId)")
    long countTotal(@Param("schoolId") Long schoolId);

    /**
     * Status-based operator count (ACTIVE / INACTIVE).
     */
    @Query("SELECT COUNT(c) FROM ClassroomOperator c WHERE (:schoolId IS NULL OR c.school.id = :schoolId) AND c.status = :status")
    long countByStatus(@Param("schoolId") Long schoolId, @Param("status") UserStatus status);

    /**
     * Operators created recently (e.g. past 30 days).
     */
    @Query("SELECT COUNT(c) FROM ClassroomOperator c WHERE (:schoolId IS NULL OR c.school.id = :schoolId) AND c.createdAt >= :since")
    long countRecentlyAdded(@Param("schoolId") Long schoolId, @Param("since") LocalDateTime since);

    /**
     * Count of distinct assigned classrooms.
     */
    @Query("SELECT COUNT(DISTINCT c.assignedClassroom) FROM ClassroomOperator c WHERE (:schoolId IS NULL OR c.school.id = :schoolId) AND c.assignedClassroom IS NOT NULL AND TRIM(c.assignedClassroom) <> ''")
    long countDistinctAssignedClassrooms(@Param("schoolId") Long schoolId);
}

