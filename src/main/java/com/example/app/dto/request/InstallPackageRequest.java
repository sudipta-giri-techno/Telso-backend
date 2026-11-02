package com.example.app.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InstallPackageRequest {
    @NotNull
    private Long operatorId;
}
