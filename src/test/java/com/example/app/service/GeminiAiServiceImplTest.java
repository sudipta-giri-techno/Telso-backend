package com.example.app.service;

import com.example.app.dto.request.AskQuestionRequest;
import com.example.app.dto.response.AskQuestionResponse;
import com.example.app.entity.Content;
import com.example.app.repository.ContentRepository;
import com.example.app.service.impl.GeminiAiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiAiServiceImplTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private ContentRepository contentRepository;

    private GeminiAiServiceImpl geminiAiService;

    @BeforeEach
    void setUp() {
        geminiAiService = new GeminiAiServiceImpl(chatClient, contentRepository);
        ReflectionTestUtils.setField(geminiAiService, "apiKey", "test-valid-key");
        ReflectionTestUtils.setField(geminiAiService, "modelName", "gemini-1.5-flash");
    }

    @Test
    @DisplayName("Should answer question and attach content sources using Spring AI ChatClient")
    void testAnswerQuestion_Success() {
        Content mockContent = Content.builder()
                .id(1L)
                .title("Photosynthesis Overview")
                .subject("Science")
                .targetClass("Class 10")
                .chapter("Life Processes")
                .topic("Photosynthesis")
                .active(true)
                .build();

        when(contentRepository.findWithFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockContent)));

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Photosynthesis produces glucose and oxygen.");

        AskQuestionRequest request = AskQuestionRequest.builder()
                .question("What is photosynthesis?")
                .subject("Science")
                .targetClass("Class 10")
                .build();

        AskQuestionResponse response = geminiAiService.answerQuestion(request);

        assertNotNull(response);
        assertEquals("Photosynthesis produces glucose and oxygen.", response.getAnswer());
        assertEquals("98%", response.getConfidenceScore());
        assertEquals("gemini-1.5-flash", response.getModelUsed());
        assertEquals(1, response.getSources().size());
        assertEquals("Photosynthesis Overview", response.getSources().get(0).getTitle());
    }

    @Test
    @DisplayName("Should return guidance response when API key is missing")
    void testAnswerQuestion_MissingApiKey() {
        ReflectionTestUtils.setField(geminiAiService, "apiKey", "");

        AskQuestionRequest request = AskQuestionRequest.builder()
                .question("What is photosynthesis?")
                .build();

        AskQuestionResponse response = geminiAiService.answerQuestion(request);

        assertNotNull(response);
        assertTrue(response.getAnswer().contains("Gemini API key is not configured"));
        assertEquals("0%", response.getConfidenceScore());
    }
}

