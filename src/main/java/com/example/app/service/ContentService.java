package com.example.app.service;

import com.example.app.dto.request.ContentUploadRequest;
import com.example.app.dto.response.ContentLibraryResponse;
import com.example.app.dto.response.ContentResponse;
import com.example.app.enums.ContentType;
import org.springframework.web.multipart.MultipartFile;

public interface ContentService {

    ContentResponse uploadContent(ContentUploadRequest request,
                                  MultipartFile englishFile,
                                  MultipartFile manipuriFile,
                                  String uploaderEmail);

    ContentLibraryResponse getContents(
            String search,
            ContentType contentType,
            String targetClass,
            String subject,
            String language,
            int page,
            int size);

    ContentResponse getContentById(Long id);

    void deleteContent(Long id);
}