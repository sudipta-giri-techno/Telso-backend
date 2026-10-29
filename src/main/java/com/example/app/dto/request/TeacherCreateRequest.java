package com.example.app.dto.request;

import com.example.app.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * Request body for creating a new teacher.
 * classSections follow the "7B" format (class number + section letter).
 */
@Data
public class TeacherCreateRequest {

    @NotBlank(message = "Teacher name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 150, message = "Subject must be at most 150 characters")
    private String subject;

    /**
     * List of class-section strings, e.g. ["7B", "8A", "9C"].
     * The client sends class+section concatenated.
     */
    private List<String> classSections;

    private UserStatus status = UserStatus.ACTIVE;

    /** School to which this teacher belongs (resolved from logged-in admin's school). */
    private Long schoolId;
}
