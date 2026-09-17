package com.petadoption.ai.agent;

import com.petadoption.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentExecutor {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public AgentResponse execute(
            String systemInstructions,
            String conversationId,
            String userMessage,
            Object... tools) {

        try {

            String reply = chatClient.prompt()
                    .system(systemInstructions)
                    .advisors(
                            MessageChatMemoryAdvisor.builder(chatMemory)
                                    .build())
                    .advisors(a -> a.param(
                            ChatMemory.CONVERSATION_ID, conversationId))
                    .tools(tools)
                    .user(userMessage)
                    .call()
                    .content();

            return new AgentResponse(reply);

        } catch (Exception ex) {

            log.error("Agent execution failed", ex);

            throw new AiServiceException(
                    "AI service is currently unavailable. "
                            + "Please try again later.");
        }
    }
}
