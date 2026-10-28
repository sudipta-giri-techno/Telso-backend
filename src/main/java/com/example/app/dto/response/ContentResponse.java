package com.example.app.dto.response;

import com.example.app.entity.Content;
import com.example.app.enums.ContentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {

    private Long id;
    private String title;
    private String description;
    private ContentType contentType;
    private String contentTypeDisplay;

    @JsonProperty("class")
    private String targetClass;

    private String subject;
    private String chapter;
    private String topic;
    private String chapterTopic;
    private String keywords;
    private String notes;

    @Builder.Default
    private Set<String> languages = new LinkedHashSet<>();

    private Long uploaderId;
    private String uploaderName;
    private String uploaderEmail;

    private LocalDateTime createdAt;
    private LocalDateTime uploadedOn;
    private LocalDateTime updatedAt;

    public static ContentResponse from(Content content) {
        if (content == null) {
            return null;
        }

        String chapterTopic;
        if (content.getChapter() != null && !content.getChapter().isBlank()
                && content.getTopic() != null && !content.getTopic().isBlank()) {
            chapterTopic = content.getChapter() + " / " + content.getTopic();
        } else if (content.getChapter() != null && !content.getChapter().isBlank()) {
            chapterTopic = content.getChapter();
        } else if (content.getTopic() != null && !content.getTopic().isBlank()) {
            chapterTopic = content.getTopic();
        } else {
            chapterTopic = "-";
        }

        Long uploaderId = null;
        String uploaderName = null;
        String uploaderEmail = null;
        if (content.getUploadedBy() != null) {
            uploaderId = content.getUploadedBy().getId();
            uploaderName = content.getUploadedBy().getFullName();
            uploaderEmail = content.getUploadedBy().getEmail();
        }

        String typeDisplay = null;
        if (content.getContentType() != null) {
            typeDisplay = switch (content.getContentType()) {
                case TEXTBOOK -> "Textbook";
                case SYLLABUS -> "Syllabus";
                case NOTES_AND_REFERENCES -> "Notes & References";
            };
        }

        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .contentType(content.getContentType())
                .contentTypeDisplay(typeDisplay)
                .targetClass(content.getTargetClass())
                .subject(content.getSubject())
                .chapter(content.getChapter())
                .topic(content.getTopic())
                .chapterTopic(chapterTopic)
                .keywords(content.getKeywords())
                .notes(content.getNotes())
                .languages(content.getLanguages() != null ? new LinkedHashSet<>(content.getLanguages()) : new LinkedHashSet<>())
                .uploaderId(uploaderId)
                .uploaderName(uploaderName)
                .uploaderEmail(uploaderEmail)
                .createdAt(content.getCreatedAt())
                .uploadedOn(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }
}
