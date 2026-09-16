package com.petadoption.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

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
}
