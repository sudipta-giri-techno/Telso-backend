package com.example.app.dto.response;

import com.example.app.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed representation of a Section record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionResponse {

    private Long id;
    private String sectionName;
    private String name;

    /** Badge letter e.g. "A", "B" */
    private String badge;

    private String classNumeric;
    private String className;
    private Integer totalStudents;
    private UserStatus status;
    private Long schoolId;
    private String schoolName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

