package com.example.app.dto.request;

import com.example.app.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * Request body for updating an existing teacher.
 * All fields are optional; only non-null values are applied.
 */
@Data
public class TeacherUpdateRequest {

    @Size(max = 150)
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 150)
    private String subject;

    /**
     * Full replacement list of class-sections.
     * Pass the complete desired list, e.g. ["7B", "8A", "10C"].
     * Passing an empty list removes all assignments.
     */
    private List<String> classSections;

    private UserStatus status;
}
