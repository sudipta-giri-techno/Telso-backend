package com.example.app.service.impl;

import com.example.app.dto.request.ContentUploadRequest;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public ContentResponse uploadContent(ContentUploadRequest request, String uploaderEmail) {
        log.info("Uploading content title='{}', type='{}', class='{}', subject='{}' by user='{}'",
                request.getTitle(), request.getContentType(), request.getTargetClass(), request.getSubject(), uploaderEmail);

        User uploader = null;
        if (uploaderEmail != null && !uploaderEmail.isBlank()) {
            uploader = userRepository.findByEmail(uploaderEmail).orElse(null);
        }

        Set<String> resolvedLanguages = request.resolveLanguages();

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
                .languages(new LinkedHashSet<>(resolvedLanguages))
                .uploadedBy(uploader)
                .build();

        Content saved = contentRepository.save(content);
        log.info("Content created successfully with id={}", saved.getId());
        return ContentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContentResponse> getContents(
            String search,
            ContentType contentType,
            String targetClass,
            String subject,
            String language,
            int page,
            int size
    ) {
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanClass = (targetClass != null && !targetClass.isBlank() && !targetClass.equalsIgnoreCase("All Classes")) ? targetClass.trim() : null;
        String cleanSubject = (subject != null && !subject.isBlank() && !subject.equalsIgnoreCase("All Subjects")) ? subject.trim() : null;
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

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Content> contentPage = contentRepository.findWithFilters(
                cleanSearch, contentType, cleanClass, cleanSubject, cleanLang, pageable);

        List<ContentResponse> responses = contentPage.getContent().stream()
                .map(ContentResponse::from)
                .toList();

        return new PagedResponse<>(
                responses,
                contentPage.getNumber(),
                contentPage.getSize(),
                contentPage.getTotalElements(),
                contentPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));
        return ContentResponse.from(content);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentStatsResponse getContentStats() {
        long total = contentRepository.count();
        long textbooks = contentRepository.countByContentType(ContentType.TEXTBOOK);
        long syllabus = contentRepository.countByContentType(ContentType.SYLLABUS);
        long notes = contentRepository.countByContentType(ContentType.NOTES_AND_REFERENCES);

        return ContentStatsResponse.builder()
                .totalContents(total)
                .textbooks(textbooks)
                .syllabus(syllabus)
                .notesAndReferences(notes)
                .build();
    }

    @Override
    public void deleteContent(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));

        contentRepository.delete(content);
        log.info("Content deleted successfully with id={}", id);
    }
}
