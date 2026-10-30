package com.example.app.dto.request;

import com.example.app.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for updating an existing class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassUpdateRequest {

    @Size(max = 100, message = "Class name must be at most 100 characters")
    @JsonAlias({"name", "className", "class_name"})
    private String className;

    @Size(max = 100, message = "Grade level must be at most 100 characters")
    @JsonAlias({"grade", "gradeLevel", "grade_level"})
    private String gradeLevel;

    @JsonAlias({"total_sections", "totalSections"})
    private Integer totalSections;

    @JsonAlias({"total_students", "totalStudents"})
    private Integer totalStudents;

    private UserStatus status;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (name != null) {
            this.className = name;
        }
    }

    public String getName() {
        return this.className;
    }
}

