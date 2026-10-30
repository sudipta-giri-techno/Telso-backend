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
 * Request payload for creating a new section.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionCreateRequest {

    @NotBlank(message = "Section name is required")
    @Size(max = 100, message = "Section name must be at most 100 characters")
    @JsonAlias({"name", "sectionName", "section_name", "section"})
    private String sectionName;

    @NotBlank(message = "Class is required")
    @Size(max = 50, message = "Class must be at most 50 characters")
    @JsonAlias({"class", "classNumeric", "class_numeric"})
    private String classNumeric;

    @Size(max = 100, message = "Class name must be at most 100 characters")
    @JsonAlias({"className", "class_name"})
    private String className;

    @JsonAlias({"total_students", "totalStudents"})
    @Builder.Default
    private Integer totalStudents = 0;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (this.sectionName == null || this.sectionName.isBlank()) {
            this.sectionName = name;
        }
    }

    public String getName() {
        return this.sectionName;
    }
}

