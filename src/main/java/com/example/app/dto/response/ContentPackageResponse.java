package com.example.app.dto.response;

import com.example.app.enums.ContentInstallationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPackageResponse {
    private Long contentId;
    private String packageName;          // content.title
    private String description;
    private String className;            // content.targetClass
    private String subject;
    private Set<String> languages;
    private String version;
    private String publishedBy;          // uploadedBy user's name/email
    @JsonFormat(pattern = "dd MMM yyyy, hh:mm a")
    private LocalDateTime publishedOn;   // content.createdAt
    private ContentInstallationStatus installStatus;
    private Long installationId;         // null if not yet tracked
}

