package com.example.app.dto.response;

import com.example.app.enums.Gender;
import com.example.app.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Detailed representation of a Student record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long id;

    private String rollNumber;

    private String studentName;

    private String name;

    /** 2-letter initials for avatar badge (e.g., "AR", "VK") */
    private String initials;

    private String className;

    private String section;

    private Gender gender;

    private LocalDate dateOfBirth;

    /** Formatted date of birth string (e.g. "12 Feb 2011") */
    private String dateOfBirthFormatted;

    private String parentName;

    private String parentPhone;

    /** Display combination of parent name and phone */
    private String parentGuardianDisplay;

    private UserStatus status;

    private Long schoolId;

    private String schoolName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

