package com.petadoption.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

    private static final int MAX_MEMORY_MESSAGES = 20;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {

        return builder
                .defaultSystem(
                        "You are a helpful assistant for a pet "
                                + "adoption platform. Keep answers "
                                + "concise and relevant to pet "
                                + "adoption, pet care, and the "
                                + "platform's services.")
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(MAX_MEMORY_MESSAGES)
                .build();
    }
}
