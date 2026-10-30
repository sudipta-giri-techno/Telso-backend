package com.example.app.controller;

import com.example.app.dto.request.QuestionCreateRequest;
import com.example.app.dto.request.QuestionSetCreateRequest;
import com.example.app.dto.request.QuestionSetUpdateRequest;
import com.example.app.dto.request.QuestionUpdateRequest;
import com.example.app.entity.Question;
import com.example.app.entity.QuestionSet;
import com.example.app.entity.User;
import com.example.app.enums.*;
import com.example.app.repository.QuestionRepository;
import com.example.app.repository.QuestionSetRepository;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuestionSetControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionSetRepository questionSetRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User contentAdminUser;
    private String contentAdminToken;

    private User centralAdminUser;
    private String centralAdminToken;

    private User regularUser;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        questionSetRepository.deleteAll();

        contentAdminUser = createOrGetUser("contentadmin_qs@telso.in", Role.CONTENT_ADMIN);
        contentAdminToken = jwtTokenProvider.generateAccessToken(contentAdminUser);

        centralAdminUser = createOrGetUser("centraladmin_qs@telso.in", Role.CENTRAL_ADMIN);
        centralAdminToken = jwtTokenProvider.generateAccessToken(centralAdminUser);

        regularUser = createOrGetUser("regularuser_qs@telso.in", Role.USER);
        regularUserToken = jwtTokenProvider.generateAccessToken(regularUser);
    }

    private User createOrGetUser(String email, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = User.builder()
                    .firstName("Test")
                    .lastName(role.name())
                    .email(email)
                    .password(passwordEncoder.encode("Password@123"))
                    .role(role)
                    .status(UserStatus.ACTIVE)
                    .build();
            return userRepository.save(user);
        });
    }

    @Test
    @DisplayName("Content Admin can create a question set without questions")
    void createQuestionSet_withoutQuestions_success() throws Exception {
        QuestionSetCreateRequest request = QuestionSetCreateRequest.builder()
                .name("Human Digestive System Assessment")
                .targetClass("8")
                .subject("Science")
                .chapter("Human Body")
                .topic("Digestive System")
                .languages(Set.of("English", "Manipuri"))
                .numberOfQuestions(25)
                .questionTypes(Set.of(QuestionType.SHORT_ANSWER, QuestionType.MCQ))
                .difficultyLevels(Set.of(DifficultyLevel.EASY, DifficultyLevel.MEDIUM))
                .bloomsTaxonomy(Set.of(BloomsTaxonomy.REMEMBER, BloomsTaxonomy.UNDERSTAND))
                .status(QuestionSetStatus.READY)
                .tags(Set.of("practice", "exam"))
                .build();

        mockMvc.perform(post("/api/question-sets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.setId").value("QS-0001"))
                .andExpect(jsonPath("$.name").value("Human Digestive System Assessment"))
                .andExpect(jsonPath("$.class").value("8"))
                .andExpect(jsonPath("$.subject").value("Science"))
                .andExpect(jsonPath("$.chapter").value("Human Body"))
                .andExpect(jsonPath("$.topic").value("Digestive System"))
                .andExpect(jsonPath("$.chapterTopic").value("Human Body / Digestive System"))
                .andExpect(jsonPath("$.languages", containsInAnyOrder("EN", "MR")))
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.totalQuestions").value(0));
    }

    @Test
    @DisplayName("Content Admin can create a question set WITH initial questions (Requirement 2)")
    void createQuestionSet_withQuestions_success() throws Exception {
        QuestionCreateRequest q1 = QuestionCreateRequest.builder()
                .orderIndex(1)
                .question("What is digestion?")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(DifficultyLevel.EASY)
                .languages(Set.of("EN", "MR"))
                .source("Science Textbook Pg. 32")
                .explanation("Digestion is the breakdown of large insoluble food molecules into small water-soluble food molecules.")
                .build();

        QuestionCreateRequest q2 = QuestionCreateRequest.builder()
                .orderIndex(2)
                .question("Which organ produces bile?")
                .type(QuestionType.MCQ)
                .difficulty(DifficultyLevel.EASY)
                .languages(Set.of("EN"))
                .source("Science Textbook Pg. 36")
                .options(List.of("Liver", "Stomach", "Pancreas", "Kidney"))
                .correctAnswer("Liver")
                .build();

        QuestionSetCreateRequest request = QuestionSetCreateRequest.builder()
                .name("Human Digestive System Assessment")
                .targetClass("8")
                .subject("Science")
                .chapter("Human Body")
                .topic("Digestive System")
                .languages(Set.of("EN", "MR"))
                .numberOfQuestions(2)
                .status(QuestionSetStatus.READY)
                .questions(List.of(q1, q2))
                .build();

        mockMvc.perform(post("/api/question-sets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.approvedQuestions").value(2))
                .andExpect(jsonPath("$.questions", hasSize(2)))
                .andExpect(jsonPath("$.questions[0].question").value("What is digestion?"))
                .andExpect(jsonPath("$.questions[0].type").value("SHORT_ANSWER"))
                .andExpect(jsonPath("$.questions[0].source").value("Science Textbook Pg. 32"))
                .andExpect(jsonPath("$.questions[1].question").value("Which organ produces bile?"))
                .andExpect(jsonPath("$.questions[1].options", contains("Liver", "Stomach", "Pancreas", "Kidney")));
    }

    @Test
    @DisplayName("Content Admin can further add a question to an existing question set (Requirement 2)")
    void addQuestionToQuestionSet_success() throws Exception {
        QuestionSet set = createSampleQuestionSet("Human Digestive System Assessment", "8", "Science", QuestionSetStatus.READY);

        QuestionCreateRequest newQuestion = QuestionCreateRequest.builder()
                .question("Why is the small intestine longer than the large intestine?")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(DifficultyLevel.MEDIUM)
                .source("Science Textbook Pg. 38")
                .build();

        mockMvc.perform(post("/api/question-sets/" + set.getId() + "/questions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newQuestion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.question").value("Why is the small intestine longer than the large intestine?"))
                .andExpect(jsonPath("$.type").value("SHORT_ANSWER"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.orderIndex").value(1));

        // Verify set now has 1 question
        mockMvc.perform(get("/api/question-sets/" + set.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuestions").value(1));
    }

    @Test
    @DisplayName("Edit option: Content Admin can edit question set and add/replace questions (Requirement 2)")
    void updateQuestionSet_editOption_addsQuestions() throws Exception {
        QuestionSet set = createSampleQuestionSet("Original Name", "8", "Science", QuestionSetStatus.DRAFT);

        QuestionCreateRequest q1 = QuestionCreateRequest.builder()
                .question("Name the main organs of the digestive system.")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(DifficultyLevel.EASY)
                .build();

        QuestionSetUpdateRequest updateReq = QuestionSetUpdateRequest.builder()
                .name("Updated Assessment Name")
                .status(QuestionSetStatus.READY)
                .questions(List.of(q1))
                .build();

        mockMvc.perform(put("/api/question-sets/" + set.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Assessment Name"))
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.totalQuestions").value(1))
                .andExpect(jsonPath("$.questions[0].question").value("Name the main organs of the digestive system."));
    }

    @Test
    @DisplayName("Content Admin can edit an existing question inside a question set")
    void updateQuestion_success() throws Exception {
        QuestionSet set = createSampleQuestionSet("Assessment", "8", "Science", QuestionSetStatus.READY);

        Question q = questionRepository.save(Question.builder()
                .questionSet(set)
                .questionText("Initial Question?")
                .questionType(QuestionType.SHORT_ANSWER)
                .difficultyLevel(DifficultyLevel.EASY)
                .orderIndex(1)
                .build());

        QuestionUpdateRequest updateRequest = QuestionUpdateRequest.builder()
                .question("Updated Question text?")
                .difficulty(DifficultyLevel.HARD)
                .source("Science Textbook Pg. 40")
                .build();

        mockMvc.perform(put("/api/question-sets/" + set.getId() + "/questions/" + q.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("Updated Question text?"))
                .andExpect(jsonPath("$.difficulty").value("HARD"))
                .andExpect(jsonPath("$.source").value("Science Textbook Pg. 40"));
    }

    @Test
    @DisplayName("Content Admin can delete an existing question from a question set")
    void deleteQuestion_success() throws Exception {
        QuestionSet set = createSampleQuestionSet("Assessment", "8", "Science", QuestionSetStatus.READY);

        Question q = questionRepository.save(Question.builder()
                .questionSet(set)
                .questionText("To be deleted?")
                .questionType(QuestionType.SHORT_ANSWER)
                .difficultyLevel(DifficultyLevel.EASY)
                .orderIndex(1)
                .build());

        mockMvc.perform(delete("/api/question-sets/" + set.getId() + "/questions/" + q.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken))
                .andExpect(status().isNoContent());

        assertFalse(questionRepository.existsById(q.getId()));
    }

    @Test
    @DisplayName("List question sets with search and filters (Image 2)")
    void getQuestionSets_withFilters_success() throws Exception {
        createSampleQuestionSet("Human Digestive System Assessment", "8", "Science", QuestionSetStatus.READY);
        createSampleQuestionSet("Photosynthesis Practice Set", "8", "Science", QuestionSetStatus.READY);
        createSampleQuestionSet("Algebra Basics Quiz", "7", "Mathematics", QuestionSetStatus.DRAFT);

        // Filter by class 8
        mockMvc.perform(get("/api/question-sets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .param("class", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));

        // Filter by status DRAFT
        mockMvc.perform(get("/api/question-sets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Algebra Basics Quiz"));

        // Search by keyword "Photosynthesis"
        mockMvc.perform(get("/api/question-sets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .param("search", "Photosynthesis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Photosynthesis Practice Set"));
    }

    @Test
    @DisplayName("Get paginated questions under a question set with search & filters (Image 3)")
    void getQuestionsBySetId_withFilters_success() throws Exception {
        QuestionSet set = createSampleQuestionSet("Assessment", "8", "Science", QuestionSetStatus.READY);

        Question q1 = Question.builder()
                .questionText("What is digestion?")
                .questionType(QuestionType.SHORT_ANSWER)
                .difficultyLevel(DifficultyLevel.EASY)
                .orderIndex(1)
                .build();
        Question q2 = Question.builder()
                .questionText("Which organ produces bile?")
                .questionType(QuestionType.MCQ)
                .difficultyLevel(DifficultyLevel.EASY)
                .orderIndex(2)
                .build();
        Question q3 = Question.builder()
                .questionText("Explain the process of digestion in brief.")
                .questionType(QuestionType.LONG_ANSWER)
                .difficultyLevel(DifficultyLevel.MEDIUM)
                .orderIndex(3)
                .build();

        set.addQuestion(q1);
        set.addQuestion(q2);
        set.addQuestion(q3);
        questionSetRepository.save(set);

        // Filter by type MCQ
        mockMvc.perform(get("/api/question-sets/" + set.getId() + "/questions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .param("type", "MCQ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.content[0].question").value("Which organ produces bile?"));

        // Filter by difficulty MEDIUM
        mockMvc.perform(get("/api/question-sets/" + set.getId() + "/questions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken)
                        .param("difficulty", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.content[0].question").value("Explain the process of digestion in brief."));
    }

    @Test
    @DisplayName("Delete question set cascades and deletes associated questions")
    void deleteQuestionSet_success() throws Exception {
        QuestionSet set = createSampleQuestionSet("To Delete", "8", "Science", QuestionSetStatus.DRAFT);
        Question q = questionRepository.save(Question.builder()
                .questionSet(set)
                .questionText("Question 1")
                .questionType(QuestionType.SHORT_ANSWER)
                .orderIndex(1)
                .build());

        Long setId = set.getId();
        Long qId = q.getId();

        mockMvc.perform(delete("/api/question-sets/" + setId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contentAdminToken))
                .andExpect(status().isNoContent());

        assertFalse(questionSetRepository.existsById(setId));
        assertFalse(questionRepository.existsById(qId));
    }

    @Test
    @DisplayName("Security: Regular USER role cannot create or modify question sets (403 Forbidden)")
    void security_regularUserBlocked() throws Exception {
        QuestionSetCreateRequest request = QuestionSetCreateRequest.builder()
                .name("Unauthorized Set")
                .targetClass("8")
                .subject("Science")
                .chapter("Biology")
                .build();

        mockMvc.perform(post("/api/question-sets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Unauthenticated request is rejected (401 Unauthorized)")
    void security_unauthenticatedBlocked() throws Exception {
        mockMvc.perform(get("/api/question-sets"))
                .andExpect(status().isUnauthorized());
    }

    private QuestionSet createSampleQuestionSet(String name, String targetClass, String subject, QuestionSetStatus status) {
        long count = questionSetRepository.count() + 1;
        QuestionSet set = QuestionSet.builder()
                .setId(String.format("QS-%04d", count))
                .name(name)
                .targetClass(targetClass)
                .subject(subject)
                .chapter("Chapter 1")
                .topic("Topic 1")
                .languages(new LinkedHashSet<>(Set.of("EN", "MR")))
                .numberOfQuestions(10)
                .status(status)
                .createdBy(contentAdminUser)
                .build();
        return questionSetRepository.save(set);
    }
}