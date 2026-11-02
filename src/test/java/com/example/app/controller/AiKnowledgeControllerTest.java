package com.example.app.controller;

import com.example.app.dto.request.AskQuestionRequest;
import com.example.app.dto.response.AskQuestionResponse;
import com.example.app.service.AiKnowledgeService;
import com.example.app.service.AiKnowledgeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiKnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiKnowledgeService aiKnowledgeService;

    @Test
    @DisplayName("Content Admin can ask questions to Gemini AI")
    @WithMockUser(username = "contentadmin@test.com", roles = {"CONTENT_ADMIN"})
    void testAskQuestion_ContentAdmin_Success() throws Exception {
        AskQuestionRequest request = AskQuestionRequest.builder()
                .question("What is photosynthesis?")
                .build();

        AskQuestionResponse mockResponse = AskQuestionResponse.builder()
                .answer("Photosynthesis is the process by which green plants make food.")
                .confidenceScore("98%")
                .modelUsed("gemini-1.5-flash")
                .sources(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .build();

        Mockito.when(aiKnowledgeService.answerQuestion(any(AskQuestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Photosynthesis is the process by which green plants make food."))
                .andExpect(jsonPath("$.confidenceScore").value("98%"))
                .andExpect(jsonPath("$.modelUsed").value("gemini-1.5-flash"));
    }

    @Test
    @DisplayName("Unauthorized user without CONTENT_ADMIN or CENTRAL_ADMIN cannot access AI Q&A")
    @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
    void testAskQuestion_Forbidden_ForTeacher() throws Exception {
        AskQuestionRequest request = AskQuestionRequest.builder()
                .question("What is photosynthesis?")
                .build();

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

