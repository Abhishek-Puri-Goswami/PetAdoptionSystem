package com.petadoption.ai.advisor;

import com.petadoption.ai.agent.AgentExecutor;
import com.petadoption.ai.agent.AgentResponse;
import com.petadoption.ai.agent.AiConversationIds;
import com.petadoption.ai.tool.PetSearchTools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetMatchingAdvisorServiceTest {

    @Mock
    private AgentExecutor agentExecutor;

    @Mock
    private PetSearchTools petSearchTools;

    @Mock
    private AiConversationIds conversationIds;

    @InjectMocks
    private PetMatchingAdvisorService petMatchingAdvisorService;

    @Test
    void shouldUseAdvisorNamespacedUserKey() {

        when(conversationIds.forCurrentUser("pet-matching:"))
                .thenReturn("pet-matching:4");

        when(agentExecutor.execute(
                any(),
                eq("pet-matching:4"),
                eq("Find me a calm dog"),
                eq(petSearchTools)))
                .thenReturn(new AgentResponse(
                        "Luna is a calm dog available now."));

        String reply = petMatchingAdvisorService.ask(
                "Find me a calm dog");

        assertEquals(
                "Luna is a calm dog available now.",
                reply);
    }
}
