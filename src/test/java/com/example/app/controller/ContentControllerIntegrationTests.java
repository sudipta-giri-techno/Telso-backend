package com.example.app.controller;

import com.example.app.entity.Content;
import com.example.app.entity.User;
import com.example.app.enums.ContentType;
import com.example.app.enums.Role;
import com.example.app.enums.UserStatus;
import com.example.app.repository.ContentRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test uploads go to target/, not the real uploads/ folder.
@SpringBootTest(properties = "app.upload.dir=target/test-uploads")
@AutoConfigureMockMvc
@Transactional
class ContentControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentRepository contentRepository;

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
        contentRepository.deleteAll();

        contentAdminUser =
                createOrGetUser("contentadmin@telso.in", Role.CONTENT_ADMIN);
        contentAdminToken =
                jwtTokenProvider.generateAccessToken(contentAdminUser);

        centralAdminUser =
                createOrGetUser("centraladmin@telso.in", Role.CENTRAL_ADMIN);
        centralAdminToken =
                jwtTokenProvider.generateAccessToken(centralAdminUser);

        regularUser =
                createOrGetUser("regularuser@telso.in", Role.USER);
        regularUserToken =
                jwtTokenProvider.generateAccessToken(regularUser);
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

    // ---------- multipart helpers ----------

    private MockMultipartFile dataPart(Map<String, Object> body) throws Exception {
        return new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(body));
    }

    private MockMultipartFile pdfPart(String partName, String fileName) {
        return new MockMultipartFile(
                partName, fileName, "application/pdf",
                "%PDF-1.4 test content".getBytes());
    }

    // ---------- upload tests ----------

    @Test
    @DisplayName("Content Admin uploads content with English and Manipuri")
    void contentAdminCanUploadContentWithBothLanguages() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Science Textbook");
        body.put("description", "Standard science textbook for class 8");
        body.put("contentType", "TEXTBOOK");
        body.put("targetClass", "8");
        body.put("subject", "Science");
        body.put("chapter", "Human Body");
        body.put("topic", "Digestive System");
        body.put("keywords", "digestive, biology, human body");
        body.put("notes", "Uploaded initial version");
        body.put("languages", List.of("English", "Manipuri"));

        mockMvc.perform(multipart("/api/contents")
                        .file(dataPart(body))
                        .file(pdfPart("englishFile", "science-en.pdf"))
                        .file(pdfPart("manipuriFile", "science-mr.pdf"))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Science Textbook"))
                .andExpect(jsonPath("$.contentType").value("TEXTBOOK"))
                // The existing response contract uses "class".
                .andExpect(jsonPath("$.class").value("8"))
                .andExpect(jsonPath("$.subject").value("Science"))
                .andExpect(jsonPath("$.chapter").value("Human Body"))
                .andExpect(jsonPath("$.topic").value("Digestive System"))
                .andExpect(jsonPath("$.chapterTopic")
                        .value("Human Body / Digestive System"))
                .andExpect(jsonPath("$.languages",
                        containsInAnyOrder("EN", "MR")))
                .andExpect(jsonPath("$.uploaderEmail")
                        .value(contentAdminUser.getEmail()));

        assertEquals(1, contentRepository.count());
    }

    @Test
    @DisplayName("Content Admin uploads content with English only")
    void contentAdminCanUploadContentWithSingleLanguage() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "History Textbook");
        body.put("contentType", "TEXTBOOK");
        body.put("targetClass", "9");
        body.put("subject", "History");
        body.put("chapter", "Medieval India");
        body.put("topic", "Delhi Sultanate");
        body.put("languages", List.of("English"));

        mockMvc.perform(multipart("/api/contents")
                        .file(dataPart(body))
                        .file(pdfPart("englishFile", "history-en.pdf"))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("History Textbook"))
                .andExpect(jsonPath("$.languages", contains("EN")));
    }

    @Test
    @DisplayName("Languages are inferred from uploaded files when not provided")
    void languagesAreInferredFromUploadedFiles() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Manipuri Reader");
        body.put("contentType", "TEXTBOOK");
        body.put("targetClass", "6");
        body.put("subject", "Manipuri");

        mockMvc.perform(multipart("/api/contents")
                        .file(dataPart(body))
                        .file(pdfPart("manipuriFile", "reader-mr.pdf"))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.languages", contains("MR")));
    }

    @Test
    @DisplayName("Central Admin cannot upload content")
    void centralAdminCannotUploadContent() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Central Admin Attempt");
        body.put("contentType", "TEXTBOOK");
        body.put("targetClass", "9");
        body.put("subject", "History");
        body.put("languages", List.of("English"));

        mockMvc.perform(multipart("/api/contents")
                        .file(dataPart(body))
                        .file(pdfPart("englishFile", "attempt.pdf"))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + centralAdminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Regular User cannot upload content")
    void regularUserCannotUploadContent() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "User Attempt");
        body.put("contentType", "SYLLABUS");
        body.put("targetClass", "10");
        body.put("subject", "Physics");

        mockMvc.perform(multipart("/api/contents")
                        .file(dataPart(body))
                        .file(pdfPart("englishFile", "attempt.pdf"))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to upload content is rejected")
    void unauthenticatedCannotUploadContent() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Unauthenticated Attempt");
        body.put("contentType", "TEXTBOOK");
        body.put("targetClass", "10");
        body.put("subject", "Science");

        mockMvc.perform(multipart("/api/contents")
                        .file(dataPart(body))
                        .file(pdfPart("englishFile", "attempt.pdf")))
                .andExpect(status().isUnauthorized());
    }

    // ---------- library tests ----------

    @Test
    @DisplayName("Content library supports filtering and pagination")
    void getContentsWithFiltersAndPagination() throws Exception {

        createSampleContent(
                "Science Textbook", ContentType.TEXTBOOK,
                "8", "Science", Set.of("EN", "MR"));

        createSampleContent(
                "Mathematics Textbook", ContentType.TEXTBOOK,
                "7", "Mathematics", Set.of("EN"));

        createSampleContent(
                "Syllabus 2024-25", ContentType.SYLLABUS,
                "6 - 10", "All Subjects", Set.of("EN", "MR"));

        createSampleContent(
                "Manipuri Grammar Notes", ContentType.NOTES_AND_REFERENCES,
                "6", "Manipuri", Set.of("MR"));

        // 1. All contents
        mockMvc.perform(get("/api/contents")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents.totalItems").value(4))
                .andExpect(jsonPath("$.contents.content", hasSize(4)));

        // 2. Filter by content type
        mockMvc.perform(get("/api/contents")
                        .param("contentType", "TEXTBOOK")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents.totalItems").value(2))
                .andExpect(jsonPath("$.contents.content[0].contentType")
                        .value("TEXTBOOK"))
                .andExpect(jsonPath("$.contents.content[1].contentType")
                        .value("TEXTBOOK"));

        // 3. Filter by class
        mockMvc.perform(get("/api/contents")
                        .param("class", "8")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents.totalItems").value(1))
                .andExpect(jsonPath("$.contents.content[0].title")
                        .value("Science Textbook"));

        // 4. Filter by subject
        mockMvc.perform(get("/api/contents")
                        .param("subject", "Manipuri")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents.totalItems").value(1))
                .andExpect(jsonPath("$.contents.content[0].title")
                        .value("Manipuri Grammar Notes"));

        // 5. Filter by language
        mockMvc.perform(get("/api/contents")
                        .param("language", "MR")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents.totalItems").value(3));

        // 6. Search
        mockMvc.perform(get("/api/contents")
                        .param("search", "Grammar")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents.totalItems").value(1))
                .andExpect(jsonPath("$.contents.content[0].title")
                        .value("Manipuri Grammar Notes"));
    }

    @Test
    @DisplayName("Central Admin can view the content library")
    void centralAdminCanViewContentLibrary() throws Exception {

        mockMvc.perform(get("/api/contents")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + centralAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Show single content item by ID")
    void getSingleContentById() throws Exception {

        Content content = createSampleContent(
                "History Textbook", ContentType.TEXTBOOK,
                "9", "History", Set.of("EN"));

        mockMvc.perform(get("/api/contents/" + content.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(content.getId()))
                .andExpect(jsonPath("$.title").value("History Textbook"))
                .andExpect(jsonPath("$.subject").value("History"));
    }

    @Test
    @DisplayName("Content Admin can soft-delete content")
    void contentAdminCanDeleteContent() throws Exception {

        Content content = createSampleContent(
                "Delete Me", ContentType.TEXTBOOK,
                "9", "Art", Set.of("EN"));

        mockMvc.perform(delete("/api/contents/" + content.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isNoContent());

        // Soft deletion keeps the row but marks it inactive.
        assertTrue(contentRepository.existsById(content.getId()));

        Content deletedContent = contentRepository.findById(content.getId())
                .orElseThrow();

        assertFalse(deletedContent.isActive());
    }

    private Content createSampleContent(
            String title,
            ContentType type,
            String targetClass,
            String subject,
            Set<String> languages) {

        Content content = Content.builder()
                .title(title)
                .contentType(type)
                .targetClass(targetClass)
                .subject(subject)
                .languages(new LinkedHashSet<>(languages))
                .uploadedBy(contentAdminUser)
                .build();

        return contentRepository.save(content);
    }
}