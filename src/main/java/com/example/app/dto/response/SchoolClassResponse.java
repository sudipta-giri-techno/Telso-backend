package com.example.app.dto.response;

import com.example.app.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed representation of a School Class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassResponse {

    private Long id;
    private String className;
    private String name;
    private String gradeLevel;
    private Integer totalSections;
    private Integer totalStudents;
    private UserStatus status;
    private Long schoolId;
    private String schoolName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

