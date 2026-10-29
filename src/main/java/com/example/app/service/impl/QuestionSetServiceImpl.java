package com.example.app.service.impl;

import com.example.app.dto.request.QuestionCreateRequest;
import com.example.app.dto.request.QuestionSetCreateRequest;
import com.example.app.dto.request.QuestionSetUpdateRequest;
import com.example.app.dto.request.QuestionUpdateRequest;
import com.example.app.dto.response.PagedResponse;
import com.example.app.dto.response.QuestionResponse;
import com.example.app.dto.response.QuestionSetListResponse;
import com.example.app.dto.response.QuestionSetResponse;
import com.example.app.entity.Question;
import com.example.app.entity.QuestionSet;
import com.example.app.entity.User;
import com.example.app.enums.DifficultyLevel;
import com.example.app.enums.QuestionSetStatus;
import com.example.app.enums.QuestionType;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.QuestionRepository;
import com.example.app.repository.QuestionSetRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.QuestionSetService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionSetServiceImpl implements QuestionSetService {

    private static final Logger log = LoggerFactory.getLogger(QuestionSetServiceImpl.class);

    private final QuestionSetRepository questionSetRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Override
    public QuestionSetResponse createQuestionSet(QuestionSetCreateRequest request, String creatorEmail) {
        log.info("Creating question set: name='{}', class='{}', subject='{}' by user='{}'",
                request.getName(), request.getTargetClass(), request.getSubject(), creatorEmail);

        User creator = null;
        if (creatorEmail != null && !creatorEmail.isBlank()) {
            creator = userRepository.findByEmail(creatorEmail).orElse(null);
        }

        Set<String> resolvedLanguages = request.resolveLanguages();
        if (resolvedLanguages.isEmpty()) {
            resolvedLanguages.add("EN");
        }

        String setId = request.getSetId();
        if (setId == null || setId.isBlank()) {
            setId = generateNextSetId();
        } else {
            setId = setId.trim();
        }

        int targetNumQuestions = request.getNumberOfQuestions() != null
                ? request.getNumberOfQuestions()
                : (request.getQuestions() != null && !request.getQuestions().isEmpty() ? request.getQuestions().size() : 25);

        QuestionSet questionSet = QuestionSet.builder()
                .setId(setId)
                .name(request.getName().trim())
                .targetClass(request.getTargetClass().trim())
                .subject(request.getSubject().trim())
                .chapter(request.getChapter().trim())
                .topic(request.getTopic() != null && !request.getTopic().isBlank() ? request.getTopic().trim() : null)
                .languages(new LinkedHashSet<>(resolvedLanguages))
                .numberOfQuestions(targetNumQuestions)
                .questionTypes(request.getQuestionTypes() != null ? new LinkedHashSet<>(request.getQuestionTypes()) : new LinkedHashSet<>())
                .difficultyLevels(request.getDifficultyLevels() != null ? new LinkedHashSet<>(request.getDifficultyLevels()) : new LinkedHashSet<>())
                .bloomsTaxonomy(request.getBloomsTaxonomy() != null ? new LinkedHashSet<>(request.getBloomsTaxonomy()) : new LinkedHashSet<>())
                .includeExplanations(Boolean.TRUE.equals(request.getIncludeExplanations()))
                .includeSourceReference(Boolean.TRUE.equals(request.getIncludeSourceReference()))
                .randomizeOptions(Boolean.TRUE.equals(request.getRandomizeOptions()))
                .status(request.getStatus() != null ? request.getStatus() : QuestionSetStatus.DRAFT)
                .tags(request.getTags() != null ? new LinkedHashSet<>(request.getTags()) : new LinkedHashSet<>())
                .generatedBy(request.getGeneratedBy() != null && !request.getGeneratedBy().isBlank() ? request.getGeneratedBy().trim() : "AI")
                .createdBy(creator)
                .questions(new ArrayList<>())
                .build();

        // If the content admin included questions during creation:
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            int index = 1;
            for (QuestionCreateRequest qReq : request.getQuestions()) {
                Question question = buildQuestionEntity(qReq, questionSet, index++);
                questionSet.addQuestion(question);
            }
        }

        QuestionSet saved = questionSetRepository.save(questionSet);
        log.info("Question set created successfully with id={}, setId='{}'", saved.getId(), saved.getSetId());
        return QuestionSetResponse.from(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionSetListResponse getQuestionSets(
            String search,
            String targetClass,
            String subject,
            String chapter,
            String topic,
            QuestionSetStatus status,
            String language,
            int page,
            int size
    ) {
        String cleanSearch  = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanClass   = (targetClass != null && !targetClass.isBlank() && !targetClass.equalsIgnoreCase("All Classes")) ? targetClass.trim() : null;
        String cleanSubject = (subject != null && !subject.isBlank() && !subject.equalsIgnoreCase("All Subjects")) ? subject.trim() : null;
        String cleanChapter = (chapter != null && !chapter.isBlank() && !chapter.equalsIgnoreCase("All Chapters")) ? chapter.trim() : null;
        String cleanTopic   = (topic != null && !topic.isBlank() && !topic.equalsIgnoreCase("All Topics")) ? topic.trim() : null;

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
        Page<QuestionSet> setPage = questionSetRepository.findWithFilters(
                cleanSearch, cleanClass, cleanSubject, cleanChapter, cleanTopic, status, cleanLang, pageable);

        List<QuestionSetResponse> responses = setPage.getContent().stream()
                .map(set -> QuestionSetResponse.from(set, false))
                .collect(Collectors.toList());

        // Always compute live status counts (unfiltered totals)
        long total    = questionSetRepository.count();
        long ready    = questionSetRepository.countByStatus(QuestionSetStatus.READY);
        long draft    = questionSetRepository.countByStatus(QuestionSetStatus.DRAFT);
        long archived = questionSetRepository.countByStatus(QuestionSetStatus.ARCHIVED);

        return QuestionSetListResponse.builder()
                .content(responses)
                .page(setPage.getNumber())
                .size(setPage.getSize())
                .totalItems(setPage.getTotalElements())
                .totalPages(setPage.getTotalPages())
                .totalQuestionSets(total)
                .readySets(ready)
                .draftSets(draft)
                .archivedSets(archived)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionSetResponse getQuestionSetById(Long id) {
        QuestionSet set = questionSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionSet", id));
        return QuestionSetResponse.from(set, true);
    }

    @Override
    public QuestionSetResponse updateQuestionSet(Long id, QuestionSetUpdateRequest request) {
        QuestionSet set = questionSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionSet", id));

        if (request.getName() != null && !request.getName().isBlank()) {
            set.setName(request.getName().trim());
        }
        if (request.getTargetClass() != null && !request.getTargetClass().isBlank()) {
            set.setTargetClass(request.getTargetClass().trim());
        }
        if (request.getSubject() != null && !request.getSubject().isBlank()) {
            set.setSubject(request.getSubject().trim());
        }
        if (request.getChapter() != null && !request.getChapter().isBlank()) {
            set.setChapter(request.getChapter().trim());
        }
        if (request.getTopic() != null) {
            set.setTopic(!request.getTopic().isBlank() ? request.getTopic().trim() : null);
        }
        if (request.getNumberOfQuestions() != null) {
            set.setNumberOfQuestions(request.getNumberOfQuestions());
        }
        if (request.getStatus() != null) {
            set.setStatus(request.getStatus());
        }
        if (request.getIncludeExplanations() != null) {
            set.setIncludeExplanations(request.getIncludeExplanations());
        }
        if (request.getIncludeSourceReference() != null) {
            set.setIncludeSourceReference(request.getIncludeSourceReference());
        }
        if (request.getRandomizeOptions() != null) {
            set.setRandomizeOptions(request.getRandomizeOptions());
        }
        if (request.getQuestionTypes() != null) {
            set.setQuestionTypes(new LinkedHashSet<>(request.getQuestionTypes()));
        }
        if (request.getDifficultyLevels() != null) {
            set.setDifficultyLevels(new LinkedHashSet<>(request.getDifficultyLevels()));
        }
        if (request.getBloomsTaxonomy() != null) {
            set.setBloomsTaxonomy(new LinkedHashSet<>(request.getBloomsTaxonomy()));
        }
        if (request.getTags() != null) {
            set.setTags(new LinkedHashSet<>(request.getTags()));
        }
        if (request.getGeneratedBy() != null && !request.getGeneratedBy().isBlank()) {
            set.setGeneratedBy(request.getGeneratedBy().trim());
        }

        Set<String> resolvedLanguages = request.resolveLanguages();
        if (resolvedLanguages != null && !resolvedLanguages.isEmpty()) {
            set.setLanguages(new LinkedHashSet<>(resolvedLanguages));
        }

        // If replacement/additional questions are passed in the update request:
        if (request.getQuestions() != null) {
            set.getQuestions().clear();
            int index = 1;
            for (QuestionCreateRequest qReq : request.getQuestions()) {
                Question question = buildQuestionEntity(qReq, set, index++);
                set.addQuestion(question);
            }
        }

        QuestionSet updated = questionSetRepository.save(set);
        log.info("Question set updated with id={}", updated.getId());
        return QuestionSetResponse.from(updated, true);
    }

    @Override
    public void deleteQuestionSet(Long id) {
        QuestionSet set = questionSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionSet", id));
        List<Question> questions = questionRepository.findByQuestionSetIdOrderByOrderIndexAscIdAsc(id);
        if (questions != null && !questions.isEmpty()) {
            questionRepository.deleteAll(questions);
        }
        questionSetRepository.delete(set);
        log.info("Deleted question set id={}", id);
    }

    @Override
    public QuestionResponse addQuestionToSet(Long questionSetId, QuestionCreateRequest request) {
        QuestionSet set = questionSetRepository.findById(questionSetId)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionSet", questionSetId));

        int nextOrderIndex = request.getOrderIndex() != null && request.getOrderIndex() > 0
                ? request.getOrderIndex()
                : set.getTotalQuestionsCount() + 1;

        Question question = buildQuestionEntity(request, set, nextOrderIndex);
        set.addQuestion(question);

        Question saved = questionRepository.save(question);
        log.info("Added question id={} to question set id={}", saved.getId(), questionSetId);
        return QuestionResponse.from(saved);
    }

    @Override
    public List<QuestionResponse> addQuestionsToSet(Long questionSetId, List<QuestionCreateRequest> requests) {
        QuestionSet set = questionSetRepository.findById(questionSetId)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionSet", questionSetId));

        List<QuestionResponse> responses = new ArrayList<>();
        int nextOrderIndex = set.getTotalQuestionsCount() + 1;
        for (QuestionCreateRequest req : requests) {
            int order = req.getOrderIndex() != null && req.getOrderIndex() > 0
                    ? req.getOrderIndex()
                    : nextOrderIndex++;
            Question question = buildQuestionEntity(req, set, order);
            set.addQuestion(question);
            Question saved = questionRepository.save(question);
            responses.add(QuestionResponse.from(saved));
        }
        return responses;
    }

    @Override
    public QuestionResponse updateQuestion(Long questionSetId, Long questionId, QuestionUpdateRequest request) {
        Question question = questionRepository.findByIdAndQuestionSetId(questionId, questionSetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + questionId + " under question set: " + questionSetId));

        if (request.getQuestion() != null && !request.getQuestion().isBlank()) {
            question.setQuestionText(request.getQuestion().trim());
        }
        if (request.getType() != null) {
            question.setQuestionType(request.getType());
        }
        if (request.getDifficulty() != null) {
            question.setDifficultyLevel(request.getDifficulty());
        }
        if (request.getOrderIndex() != null) {
            question.setOrderIndex(request.getOrderIndex());
        }
        if (request.getSource() != null) {
            question.setSourceReference(!request.getSource().isBlank() ? request.getSource().trim() : null);
        }
        if (request.getExplanation() != null) {
            question.setExplanation(!request.getExplanation().isBlank() ? request.getExplanation().trim() : null);
        }
        if (request.getOptions() != null) {
            question.setOptions(new ArrayList<>(request.getOptions()));
        }
        if (request.getCorrectAnswer() != null) {
            question.setCorrectAnswer(!request.getCorrectAnswer().isBlank() ? request.getCorrectAnswer().trim() : null);
        }
        if (request.getIsApproved() != null) {
            question.setIsApproved(request.getIsApproved());
        }
        Set<String> resolvedLanguages = request.resolveLanguages();
        if (resolvedLanguages != null) {
            question.setLanguages(new LinkedHashSet<>(resolvedLanguages));
        }

        Question saved = questionRepository.save(question);
        log.info("Updated question id={} in question set id={}", saved.getId(), questionSetId);
        return QuestionResponse.from(saved);
    }

    @Override
    public void deleteQuestion(Long questionSetId, Long questionId) {
        Question question = questionRepository.findByIdAndQuestionSetId(questionId, questionSetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + questionId + " under question set: " + questionSetId));

        QuestionSet set = question.getQuestionSet();
        if (set != null) {
            set.removeQuestion(question);
        }
        questionRepository.delete(question);
        log.info("Deleted question id={} from question set id={}", questionId, questionSetId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<QuestionResponse> getQuestionsBySetId(
            Long questionSetId,
            String search,
            QuestionType type,
            DifficultyLevel difficulty,
            int page,
            int size
    ) {
        if (!questionSetRepository.existsById(questionSetId)) {
            throw new ResourceNotFoundException("QuestionSet", questionSetId);
        }

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "orderIndex", "id"));
        Page<Question> qPage = questionRepository.findByQuestionSetIdWithFilters(
                questionSetId, cleanSearch, type, difficulty, pageable);

        List<QuestionResponse> responses = qPage.getContent().stream()
                .map(QuestionResponse::from)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                responses,
                qPage.getNumber(),
                qPage.getSize(),
                qPage.getTotalElements(),
                qPage.getTotalPages()
        );
    }

    private Question buildQuestionEntity(QuestionCreateRequest request, QuestionSet set, int fallbackOrder) {
        int order = request.getOrderIndex() != null && request.getOrderIndex() > 0
                ? request.getOrderIndex()
                : fallbackOrder;

        Set<String> qLanguages = request.resolveLanguages();
        if (qLanguages.isEmpty() && set.getLanguages() != null) {
            qLanguages = new LinkedHashSet<>(set.getLanguages());
        }

        return Question.builder()
                .questionSet(set)
                .orderIndex(order)
                .questionText(request.getQuestion().trim())
                .questionType(request.getType())
                .difficultyLevel(request.getDifficulty() != null ? request.getDifficulty() : DifficultyLevel.MEDIUM)
                .languages(new LinkedHashSet<>(qLanguages))
                .sourceReference(request.getSource() != null && !request.getSource().isBlank() ? request.getSource().trim() : null)
                .explanation(request.getExplanation() != null && !request.getExplanation().isBlank() ? request.getExplanation().trim() : null)
                .options(request.getOptions() != null ? new ArrayList<>(request.getOptions()) : new ArrayList<>())
                .correctAnswer(request.getCorrectAnswer() != null && !request.getCorrectAnswer().isBlank() ? request.getCorrectAnswer().trim() : null)
                .isApproved(request.getIsApproved() != null ? request.getIsApproved() : true)
                .build();
    }

    private synchronized String generateNextSetId() {
        long count = questionSetRepository.count() + 1;
        String candidate = String.format("QS-%04d", count);
        while (questionSetRepository.existsBySetId(candidate)) {
            count++;
            candidate = String.format("QS-%04d", count);
        }
        return candidate;
    }
}
