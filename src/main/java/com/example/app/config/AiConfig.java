package com.example.app.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI configuration for Telso Saarthi AI Knowledge Engine.
 * Configures the fluent ChatClient bean backed by Spring AI.
 */
@Configuration
public class AiConfig {

    /**
     * Creates and registers a ChatClient bean configured with default system behavior
     * for Telso Saarthi AI Knowledge Testing.
     *
     * @param chatClientBuilder auto-configured ChatClient.Builder provided by Spring AI starter
     * @return initialized ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultSystem("""
                        You are an educational AI assistant for Telso Saarthi (Offline AI Learning Engine).
                        Your role is to assist Content Admins with Knowledge Testing and verification.
                        Provide clear, accurate, educational, and well-structured answers using bullet points
                        or paragraphs where appropriate.
                        When educational materials or curriculum references are provided in the context,
                        base your answers strictly and accurately on them.
                        """)
                .build();
    }
}

