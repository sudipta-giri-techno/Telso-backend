package com.example.app.dto.request;

import com.example.app.enums.Gender;
import com.example.app.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request body for creating a new student record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateRequest {

    @NotBlank(message = "Roll number is required")
    @Size(max = 50, message = "Roll number must be at most 50 characters")
    @JsonAlias({"roll_number", "rollNumber", "roll"})
    private String rollNumber;

    @NotBlank(message = "Student name is required")
    @Size(max = 150, message = "Student name must be at most 150 characters")
    @JsonAlias({"name", "student_name"})
    private String studentName;

    @NotBlank(message = "Class is required")
    @Size(max = 50, message = "Class must be at most 50 characters")
    @JsonAlias({"class", "className", "class_name"})
    private String className;

    @NotBlank(message = "Section is required")
    @Size(max = 20, message = "Section must be at most 20 characters")
    private String section;

    private Gender gender;

    @JsonAlias({"dob", "date_of_birth"})
    private LocalDate dateOfBirth;

    @Size(max = 150, message = "Parent name must be at most 150 characters")
    @JsonAlias({"parent_name", "guardianName", "guardian_name"})
    private String parentName;

    @Size(max = 20, message = "Parent phone must be at most 20 characters")
    @JsonAlias({"parent_phone", "guardianPhone", "guardian_phone", "phone"})
    private String parentPhone;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (this.studentName == null || this.studentName.isBlank()) {
            this.studentName = name;
        }
    }

    public String getName() {
        return this.studentName;
    }
}

