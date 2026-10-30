package com.example.app.dto.request;

import com.example.app.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request body for updating an existing classroom operator.
 * All fields are optional; only non-null values are updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomOperatorUpdateRequest {

    @Size(max = 150, message = "Operator name must be at most 150 characters")
    @JsonAlias({"name", "operator_name"})
    private String operatorName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 150, message = "Assigned classroom must be at most 150 characters")
    @JsonAlias({"classroom", "assigned_classroom"})
    private String assignedClassroom;

    private UserStatus status;

    @JsonAlias({"last_login"})
    private LocalDateTime lastLogin;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (name != null) {
            this.operatorName = name;
        }
    }

    public String getName() {
        return this.operatorName;
    }
}

