package com.example.app.dto.request;

import com.example.app.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for creating a new subject.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCreateRequest {

    @NotBlank(message = "Subject name is required")
    @Size(max = 150, message = "Subject name must be at most 150 characters")
    @JsonAlias({"name", "subjectName", "subject_name"})
    private String subjectName;

    /**
     * Assigned classes, e.g. ["Class 6", "Class 7", "Class 8"].
     */
    @JsonAlias({"classes", "assignedClasses", "assigned_classes"})
    private List<String> classes;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (this.subjectName == null || this.subjectName.isBlank()) {
            this.subjectName = name;
        }
    }

    public String getName() {
        return this.subjectName;
    }
}

