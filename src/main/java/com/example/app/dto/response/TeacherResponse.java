package com.example.app.dto.response;

import com.example.app.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO returned for a single teacher.
 */
@Data
@Builder
public class TeacherResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    /** e.g. ["7B", "8A", "9C"] */
    private List<String> classSections;
    private UserStatus status;
    private LocalDateTime lastLogin;
    private Long schoolId;
    private String schoolName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
