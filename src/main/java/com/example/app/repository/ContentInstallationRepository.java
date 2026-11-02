package com.example.app.repository;

import com.example.app.entity.ContentInstallation;
import com.example.app.enums.ContentInstallationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentInstallationRepository extends JpaRepository<ContentInstallation, Long> {

    Optional<ContentInstallation> findByOperatorIdAndContentId(Long operatorId, Long contentId);

    List<ContentInstallation> findByOperatorId(Long operatorId);

    long countByOperatorIdAndStatus(Long operatorId, ContentInstallationStatus status);

    @Query("SELECT COUNT(DISTINCT ci.content.id) FROM ContentInstallation ci WHERE ci.operator.id = :operatorId")
    long countDistinctContentByOperatorId(@Param("operatorId") Long operatorId);
}
