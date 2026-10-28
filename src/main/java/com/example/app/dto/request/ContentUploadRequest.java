package com.example.app.dto.request;

import com.example.app.enums.ContentType;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentUploadRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Content type is required")
    private ContentType contentType;

    @NotBlank(message = "Class is required")
    @Size(max = 50, message = "Class must not exceed 50 characters")
    @JsonAlias({"class", "className", "classGrade"})
    private String targetClass;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must not exceed 100 characters")
    private String subject;

    @Size(max = 150, message = "Chapter must not exceed 150 characters")
    private String chapter;

    @Size(max = 150, message = "Topic must not exceed 150 characters")
    private String topic;

    @Size(max = 500, message = "Keywords must not exceed 500 characters")
    private String keywords;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    /**
     * Selected languages: English and/or Manipuri (Monipuri).
     * User can select both or one (e.g. ["English", "Manipuri"], ["EN", "MR"]).
     */
    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    /**
     * Optional individual boolean flags for English and Manipuri options.
     */
    private Boolean english;
    private Boolean manipuri;

    public void setClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public void setClassName(String targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Resolves the selected languages into normalized set of codes (EN, MR).
     */
    public Set<String> resolveLanguages() {
        Set<String> resolved = new LinkedHashSet<>();
        if (languages != null) {
            for (String lang : languages) {
                if (lang != null && !lang.isBlank()) {
                    String upper = lang.trim().toUpperCase();
                    if (upper.startsWith("EN")) {
                        resolved.add("EN");
                    } else if (upper.startsWith("MR") || upper.startsWith("MAN") || upper.startsWith("MON")) {
                        resolved.add("MR");
                    } else {
                        resolved.add(upper);
                    }
                }
            }
        }
        if (Boolean.TRUE.equals(english)) {
            resolved.add("EN");
        }
        if (Boolean.TRUE.equals(manipuri)) {
            resolved.add("MR");
        }
        return resolved;
    }
}
