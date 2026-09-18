package com.petadoption.ai.advisor;

import com.petadoption.ai.agent.AgentExecutor;
import com.petadoption.ai.agent.AgentResponse;
import com.petadoption.ai.agent.AiConversationIds;
import com.petadoption.ai.tool.AdoptionTools;

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
class AdoptionAdvisorServiceTest {

    @Mock
    private AgentExecutor agentExecutor;

    @Mock
    private AdoptionTools adoptionTools;

    @Mock
    private AiConversationIds conversationIds;

    @InjectMocks
    private AdoptionAdvisorService adoptionAdvisorService;

    @Test
    void shouldUseAdvisorNamespacedUserKey() {

        when(conversationIds.forCurrentUser("adoption:"))
                .thenReturn("adoption:4");

        when(agentExecutor.execute(
                any(),
                eq("adoption:4"),
                eq("What's my application status?"),
                eq(adoptionTools)))
                .thenReturn(new AgentResponse(
                        "Your application for Luna is PENDING."));

        String reply = adoptionAdvisorService.ask(
                "What's my application status?");

        assertEquals(
                "Your application for Luna is PENDING.",
                reply);
    }
}
