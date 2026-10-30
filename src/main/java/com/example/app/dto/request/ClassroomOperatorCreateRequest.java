package com.example.app.dto.request;

import com.example.app.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request body for creating a new classroom operator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomOperatorCreateRequest {

    @NotBlank(message = "Operator name is required")
    @Size(max = 150, message = "Operator name must be at most 150 characters")
    @JsonAlias({"name", "operator_name"})
    private String operatorName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 150, message = "Assigned classroom must be at most 150 characters")
    @JsonAlias({"classroom", "assigned_classroom"})
    private String assignedClassroom;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @JsonAlias({"last_login"})
    private LocalDateTime lastLogin;

    @JsonAlias({"school_id"})
    private Long schoolId;

    public void setName(String name) {
        if (this.operatorName == null || this.operatorName.isBlank()) {
            this.operatorName = name;
        }
    }

    public String getName() {
        return this.operatorName;
    }
}

