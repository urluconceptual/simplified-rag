package com.unibuc.simplifiedrag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                You are a helpful assistant that answers questions
                based strictly on the provided document context.
                If the answer is not in the context, say so clearly.
                Do not make up information.
                """)
                .build();
    }
}