package com.example.app.service;

import com.example.app.dto.request.ContentUploadRequest;
import com.example.app.dto.response.ContentLibraryResponse;
import com.example.app.dto.response.ContentResponse;
import com.example.app.enums.ContentType;

public interface ContentService {

    /**
     * Uploads new educational content with selected language options (English, Manipuri, or both).
     * Restricted to CONTENT_ADMIN.
     */
    ContentResponse uploadContent(ContentUploadRequest request, String uploaderEmail);

    /**
     * Retrieves paginated, filtered content list PLUS aggregated status counts in one response.
     * Status counts always reflect the full active dataset regardless of applied filters.
     */
    ContentLibraryResponse getContents(
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
     * Soft-deletes a content item by setting active = false (no physical row removal).
     */
    void deleteContent(Long id);
}
