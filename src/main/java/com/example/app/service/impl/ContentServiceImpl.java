package com.example.app.service.impl;

import com.example.app.dto.request.ContentUploadRequest;
import com.example.app.dto.response.ContentLibraryResponse;
import com.example.app.dto.response.ContentResponse;
import com.example.app.dto.response.ContentStatsResponse;
import com.example.app.dto.response.PagedResponse;
import com.example.app.entity.Content;
import com.example.app.entity.User;
import com.example.app.enums.ContentType;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ContentRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.ContentService;
import com.example.app.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentServiceImpl implements ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentServiceImpl.class);

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    public ContentResponse uploadContent(ContentUploadRequest request,
                                         MultipartFile englishFile,
                                         MultipartFile manipuriFile,
                                         String uploaderEmail) {
        log.info("Uploading content title='{}', type='{}', class='{}', subject='{}' by user='{}'",
                request.getTitle(), request.getContentType(), request.getTargetClass(),
                request.getSubject(), uploaderEmail);

        // Remove this check if you want to allow uploads without any PDF.
        boolean hasEnglish = englishFile != null && !englishFile.isEmpty();
        boolean hasManipuri = manipuriFile != null && !manipuriFile.isEmpty();
        if (!hasEnglish && !hasManipuri) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one PDF file (englishFile or manipuriFile) is required");
        }

        User uploader = null;
        if (uploaderEmail != null && !uploaderEmail.isBlank()) {
            uploader = userRepository.findByEmail(uploaderEmail).orElse(null);
        }

        String englishPath = fileStorageService.storePdf(englishFile, "english");
        String manipuriPath = fileStorageService.storePdf(manipuriFile, "manipuri");

        Set<String> resolvedLanguages = new LinkedHashSet<>(request.resolveLanguages());
        if (resolvedLanguages.isEmpty()) {
            if (englishPath != null) {
                resolvedLanguages.add("EN");
            }
            if (manipuriPath != null) {
                resolvedLanguages.add("MR");
            }
        }

        Content content = Content.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .contentType(request.getContentType())
                .targetClass(request.getTargetClass().trim())
                .subject(request.getSubject().trim())
                .chapter(request.getChapter() != null && !request.getChapter().isBlank() ? request.getChapter().trim() : null)
                .topic(request.getTopic() != null && !request.getTopic().isBlank() ? request.getTopic().trim() : null)
                .keywords(request.getKeywords() != null && !request.getKeywords().isBlank() ? request.getKeywords().trim() : null)
                .notes(request.getNotes() != null && !request.getNotes().isBlank() ? request.getNotes().trim() : null)
                .englishFilePath(englishPath)
                .manipuriFilePath(manipuriPath)
                .languages(resolvedLanguages)
                .uploadedBy(uploader)
                .active(true)
                .build();

        Content saved = contentRepository.save(content);
        log.info("Content created successfully with id={}", saved.getId());
        return ContentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentLibraryResponse getContents(
            String search,
            ContentType contentType,
            String targetClass,
            String subject,
            String language,
            int page,
            int size
    ) {
        // --- Normalise filter params ---
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanClass = (targetClass != null && !targetClass.isBlank()
                && !targetClass.equalsIgnoreCase("All Classes")) ? targetClass.trim() : null;
        String cleanSubject = (subject != null && !subject.isBlank()
                && !subject.equalsIgnoreCase("All Subjects")) ? subject.trim() : null;
        String cleanLang = null;
        if (language != null && !language.isBlank() && !language.equalsIgnoreCase("All Languages")) {
            String upper = language.trim().toUpperCase();
            if (upper.startsWith("EN")) {
                cleanLang = "EN";
            } else if (upper.startsWith("MR") || upper.startsWith("MAN") || upper.startsWith("MON")) {
                cleanLang = "MR";
            } else {
                cleanLang = upper;
            }
        }

        // --- Paginated content list (filtered) ---
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Content> contentPage = contentRepository.findWithFilters(
                cleanSearch, contentType, cleanClass, cleanSubject, cleanLang, pageable);

        List<ContentResponse> responses = contentPage.getContent().stream()
                .map(ContentResponse::from)
                .toList();

        PagedResponse<ContentResponse> pagedResponse = new PagedResponse<>(
                responses,
                contentPage.getNumber(),
                contentPage.getSize(),
                contentPage.getTotalElements(),
                contentPage.getTotalPages()
        );

        // --- Status counts (always full dataset, not affected by filters) ---
        ContentStatsResponse stats = buildStats();

        return ContentLibraryResponse.builder()
                .stats(stats)
                .contents(pagedResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));
        if (!content.isActive()) {
            throw new ResourceNotFoundException("Content not found with id: " + id);
        }
        return ContentResponse.from(content);
    }

    @Override
    public void deleteContent(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));

        if (!content.isActive()) {
            throw new ResourceNotFoundException("Content not found with id: " + id);
        }

        // Soft delete: mark as inactive instead of physically removing the row
        content.setActive(false);
        contentRepository.save(content);
        log.info("Content soft-deleted (inactive) with id={}", id);
    }

    // --- private helpers ---

    private ContentStatsResponse buildStats() {
        long total     = contentRepository.countByActiveTrue();
        long textbooks = contentRepository.countByContentTypeAndActiveTrue(ContentType.TEXTBOOK);
        long syllabus  = contentRepository.countByContentTypeAndActiveTrue(ContentType.SYLLABUS);
        long notes     = contentRepository.countByContentTypeAndActiveTrue(ContentType.NOTES_AND_REFERENCES);

        return ContentStatsResponse.builder()
                .totalContents(total)
                .textbooks(textbooks)
                .syllabus(syllabus)
                .notesAndReferences(notes)
                .build();
    }
}