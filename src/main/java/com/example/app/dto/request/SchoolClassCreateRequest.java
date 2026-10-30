package com.example.app.dto.request;

import com.example.app.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating a new class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassCreateRequest {

    @NotBlank(message = "Class name is required")
    @Size(max = 100, message = "Class name must be at most 100 characters")
    @JsonAlias({"name", "className", "class_name"})
    private String className;

    @Size(max = 100, message = "Grade level must be at most 100 characters")
    @JsonAlias({"grade", "gradeLevel", "grade_level"})
    private String gradeLevel;

    @JsonAlias({"total_sections", "totalSections"})
    @Builder.Default
    private Integer totalSections = 0;

    @JsonAlias({"total_students", "totalStudents"})
    @Builder.Default
    private Integer totalStudents = 0;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (this.className == null || this.className.isBlank()) {
            this.className = name;
        }
    }

    public String getName() {
        return this.className;
    }
}

