package com.example.app.dto.response;

import com.example.app.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed DTO representation of a single Classroom Operator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomOperatorResponse {

    private Long id;

    private String operatorName;

    /** Alias for operatorName for generic consumer compatibility */
    private String name;

    /** 2-letter initials for avatar badge in UI (e.g., "DK", "NS") */
    private String initials;

    private String email;

    private String phone;

    private String assignedClassroom;

    private UserStatus status;

    private LocalDateTime lastLogin;

    /** Human-readable display string (e.g. "Today, 09:10 AM", "Yesterday, 04:20 PM") */
    private String lastLoginFormatted;

    private Long schoolId;

    private String schoolName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

