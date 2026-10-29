package com.example.app.repository;

import com.example.app.entity.Teacher;
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
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    boolean existsByEmail(String email);

    Optional<Teacher> findByEmail(String email);

    /**
     * Filtered paginated query for the teacher list view.
     * Filters: free-text search, subject, class-section, status, schoolId.
     */
    @Query("""
        SELECT DISTINCT t FROM Teacher t
        LEFT JOIN t.classSections cs
        WHERE t.school.id = :schoolId
        AND (:search IS NULL
                OR LOWER(t.name)    LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(t.email)   LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(t.subject) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR t.phone          LIKE CONCAT('%', CAST(:search AS string), '%'))
        AND (:subject       IS NULL OR LOWER(t.subject) = LOWER(CAST(:subject AS string)))
        AND (:classSection  IS NULL OR cs = :classSection)
        AND (:status        IS NULL OR t.status = :status)
        ORDER BY t.createdAt DESC
        """)
    Page<Teacher> findWithFilters(
        @Param("schoolId")      Long schoolId,
        @Param("search")        String search,
        @Param("subject")       String subject,
        @Param("classSection")  String classSection,
        @Param("status")        UserStatus status,
        Pageable pageable
    );

    /**
     * Same filters without pagination – used for Excel export.
     */
    @Query("""
        SELECT DISTINCT t FROM Teacher t
        LEFT JOIN t.classSections cs
        WHERE t.school.id = :schoolId
        AND (:search IS NULL
                OR LOWER(t.name)    LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(t.email)   LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR LOWER(t.subject) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                OR t.phone          LIKE CONCAT('%', CAST(:search AS string), '%'))
        AND (:subject      IS NULL OR LOWER(t.subject) = LOWER(CAST(:subject AS string)))
        AND (:classSection IS NULL OR cs = :classSection)
        AND (:status       IS NULL OR t.status = :status)
        ORDER BY t.createdAt DESC
        """)
    List<Teacher> findAllForExport(
        @Param("schoolId")      Long schoolId,
        @Param("search")        String search,
        @Param("subject")       String subject,
        @Param("classSection")  String classSection,
        @Param("status")        UserStatus status
    );

    long countBySchoolId(Long schoolId);
}
