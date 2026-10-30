package com.example.app.dto.request;

import com.example.app.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for updating an existing subject.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectUpdateRequest {

    @Size(max = 150, message = "Subject name must be at most 150 characters")
    @JsonAlias({"name", "subjectName", "subject_name"})
    private String subjectName;

    @JsonAlias({"classes", "assignedClasses", "assigned_classes"})
    private List<String> classes;

    private UserStatus status;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (name != null) {
            this.subjectName = name;
        }
    }

    public String getName() {
        return this.subjectName;
    }
}

