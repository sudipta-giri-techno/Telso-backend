
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
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

    @Test
    @DisplayName("Content Admin uploads content with both English and Manipuri language options selected")
    void contentAdminCanUploadContentWithBothLanguages() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Science Textbook");
        body.put("description", "Standard science textbook for class 8");
        body.put("contentType", "TEXTBOOK");
        body.put("class", "8");
        body.put("subject", "Science");
        body.put("chapter", "Human Body");
        body.put("topic", "Digestive System");
        body.put("keywords", "digestive, biology, human body");
        body.put("notes", "Uploaded initial version");
        body.put("languages", List.of("English", "Manipuri"));

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Science Textbook"))
                .andExpect(jsonPath("$.contentType").value("TEXTBOOK"))
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
    @DisplayName("Content Admin uploads content with only English language option selected")
    void contentAdminCanUploadContentWithSingleLanguage() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "History Textbook");
        body.put("contentType", "TEXTBOOK");
        body.put("targetClass", "9");
        body.put("subject", "History");
        body.put("chapter", "Medieval India");
        body.put("topic", "Delhi Sultanate");
        body.put("languages", List.of("English"));

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("History Textbook"))
                .andExpect(jsonPath("$.languages", contains("EN")));
    }

    @Test
    @DisplayName("Central Admin cannot upload content - restricted strictly to CONTENT_ADMIN")
    void centralAdminCannotUploadContent() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Central Admin Attempt");
        body.put("contentType", "TEXTBOOK");
        body.put("targetClass", "9");
        body.put("subject", "History");
        body.put("languages", List.of("English"));

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + centralAdminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Regular User cannot upload content - restricted to CONTENT_ADMIN")
    void regularUserCannotUploadContent() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "User Attempt");
        body.put("contentType", "SYLLABUS");
        body.put("targetClass", "10");
        body.put("subject", "Physics");

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to upload content is rejected with 401")
    void unauthenticatedCannotUploadContent() throws Exception {

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Unauthenticated Attempt");
        body.put("contentType", "TEXTBOOK");
        body.put("targetClass", "10");
        body.put("subject", "Science");

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get status of content returns correct counts for total, textbooks, syllabus, notes and references")
    void getContentStatusReturnsAccurateCounts() throws Exception {

        createSampleContent(
                "Math Textbook", ContentType.TEXTBOOK,
                "10", "Math", Set.of("EN"));

        createSampleContent(
                "Science Textbook", ContentType.TEXTBOOK,
                "9", "Science", Set.of("EN", "MR"));

        createSampleContent(
                "Annual Syllabus", ContentType.SYLLABUS,
                "6 - 10", "All Subjects", Set.of("EN", "MR"));

        createSampleContent(
                "Manipuri Notes", ContentType.NOTES_AND_REFERENCES,
                "6", "Manipuri", Set.of("MR"));

        createSampleContent(
                "Physics Formula Sheet", ContentType.NOTES_AND_REFERENCES,
                "10", "Physics", Set.of("EN"));

        mockMvc.perform(get("/api/contents/status")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContents").value(5))
                .andExpect(jsonPath("$.totalContent").value(5))
                .andExpect(jsonPath("$.textbooks").value(2))
                .andExpect(jsonPath("$.syllabus").value(1))
                .andExpect(jsonPath("$.notesAndReferences").value(2))
                .andExpect(jsonPath("$.notes").value(2));
    }

    @Test
    @DisplayName("Show content with search and filters (title, subject, class, language, pagination)")
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

        // 1. All contents (no filter)
        mockMvc.perform(get("/api/contents")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(4))
                .andExpect(jsonPath("$.content", hasSize(4)));

        // 2. Filter by contentType: TEXTBOOK
        mockMvc.perform(get("/api/contents")
                        .param("contentType", "TEXTBOOK")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.content[0].contentType")
                        .value("TEXTBOOK"))
                .andExpect(jsonPath("$.content[1].contentType")
                        .value("TEXTBOOK"));

        // 3. Filter by class: 8
        mockMvc.perform(get("/api/contents")
                        .param("class", "8")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Science Textbook"));

        // 4. Filter by subject: Manipuri
        mockMvc.perform(get("/api/contents")
                        .param("subject", "Manipuri")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Manipuri Grammar Notes"));

        // 5. Filter by language: MR
        // Three records contain MR: Science Textbook,
        // Syllabus 2024-25, and Manipuri Grammar Notes.
        mockMvc.perform(get("/api/contents")
                        .param("language", "MR")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(3));

        // 6. Search query
        mockMvc.perform(get("/api/contents")
                        .param("search", "Grammar")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Manipuri Grammar Notes"));
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
    @DisplayName("Content Admin can delete content")
    void contentAdminCanDeleteContent() throws Exception {

        Content content = createSampleContent(
                "Delete Me", ContentType.TEXTBOOK,
                "9", "Art", Set.of("EN"));

        mockMvc.perform(delete("/api/contents/" + content.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + contentAdminToken))
                .andExpect(status().isNoContent());

        assertFalse(contentRepository.existsById(content.getId()));
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
