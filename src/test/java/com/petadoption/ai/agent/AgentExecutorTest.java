package com.petadoption.ai.agent;

import com.petadoption.exception.AiServiceException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentExecutorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private ChatMemory chatMemory;

    @InjectMocks
    private AgentExecutor agentExecutor;

    @Test
    void shouldReturnReplyFromChatClient() {

        when(chatClient.prompt()
                .system("instructions")
                .advisors(org.mockito.ArgumentMatchers
                        .<org.springframework.ai.chat.client.advisor.api.Advisor>any())
                .advisors(org.mockito.ArgumentMatchers
                        .<java.util.function.Consumer<ChatClient.AdvisorSpec>>any())
                .tools()
                .user("Hello")
                .call()
                .content())
                .thenReturn("Hi there!");

        AgentResponse response = agentExecutor.execute(
                "instructions", "user@test.com", "Hello");

        assertEquals("Hi there!", response.reply());
    }

    @Test
    void shouldWrapFailuresAsAiServiceException() {

        when(chatClient.prompt()
                .system(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(
                AiServiceException.class,
                () -> agentExecutor.execute(
                        "instructions", "user@test.com", "Hello"));
    }
}
