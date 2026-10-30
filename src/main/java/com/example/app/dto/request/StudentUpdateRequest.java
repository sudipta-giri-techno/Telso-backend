package com.example.app.dto.request;

import com.example.app.enums.Gender;
import com.example.app.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request body for updating an existing student record.
 * All fields are optional; only non-null values are updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdateRequest {

    @Size(max = 50, message = "Roll number must be at most 50 characters")
    @JsonAlias({"roll_number", "rollNumber", "roll"})
    private String rollNumber;

    @Size(max = 150, message = "Student name must be at most 150 characters")
    @JsonAlias({"name", "student_name"})
    private String studentName;

    @Size(max = 50, message = "Class must be at most 50 characters")
    @JsonAlias({"class", "className", "class_name"})
    private String className;

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

    private UserStatus status;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (name != null) {
            this.studentName = name;
        }
    }

    public String getName() {
        return this.studentName;
    }
}

