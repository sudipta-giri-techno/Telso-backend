package com.example.app.dto.response;

import com.example.app.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed representation of a Subject.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private Long id;
    private String subjectName;
    private String name;
    private List<String> classes;
    private UserStatus status;
    private Long schoolId;
    private String schoolName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

