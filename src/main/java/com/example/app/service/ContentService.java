package com.example.app.service;

import com.example.app.dto.request.ContentUploadRequest;
import com.example.app.dto.response.ContentResponse;
import com.example.app.dto.response.ContentStatsResponse;
import com.example.app.dto.response.PagedResponse;
import com.example.app.enums.ContentType;

public interface ContentService {

    /**
     * Uploads new educational content with selected language options (English, Manipuri, or both).
     * Restricted to CONTENT_ADMIN.
     */
    ContentResponse uploadContent(ContentUploadRequest request, String uploaderEmail);

    /**
     * Retrieves paginated content list with filtering options.
     */
    PagedResponse<ContentResponse> getContents(
            String search,
            ContentType contentType,
            String targetClass,
            String subject,
            String language,
            int page,
            int size
    );

    /**
     * Retrieves a single content item by ID.
     */
    ContentResponse getContentById(Long id);

    /**
     * Computes statistics for contents (total contents, textbooks, syllabus, notes and references).
     */
    ContentStatsResponse getContentStats();

    /**
     * Deletes a content item.
     */
    void deleteContent(Long id);
}
