package com.petadoption.ai.coordinator;

import com.petadoption.exception.AiServiceException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchestrationPlannerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @InjectMocks
    private OrchestrationPlanner planner;

    @Test
    void shouldParseOrderedSteps() {

        List<PlanStep> steps = OrchestrationPlanner.parse(
                "MATCHING: find a calm dog\n"
                        + "ADOPTION: fee for the pet from the previous step");

        assertEquals(2, steps.size());
        assertEquals(Route.MATCHING, steps.get(0).advisor());
        assertEquals("find a calm dog", steps.get(0).question());
        assertEquals(Route.ADOPTION, steps.get(1).advisor());
    }

    @Test
    void shouldReturnEmptyPlanForNoneNullOrJunk() {

        assertTrue(OrchestrationPlanner.parse("NONE").isEmpty());
        assertTrue(OrchestrationPlanner.parse(null).isEmpty());
        assertTrue(OrchestrationPlanner.parse("no idea, sorry").isEmpty());
        assertTrue(OrchestrationPlanner.parse("DELETE: everything")
                .isEmpty());
        assertTrue(OrchestrationPlanner.parse("UNSURE: hmm").isEmpty());
        assertTrue(OrchestrationPlanner.parse("CARE:   ").isEmpty());
    }

    @Test
    void shouldDropRepeatedAdvisors() {

        List<PlanStep> steps = OrchestrationPlanner.parse(
                "CARE: q1\nCARE: q2\nADOPTION: q3");

        assertEquals(2, steps.size());
        assertEquals("q1", steps.get(0).question());
        assertEquals(Route.ADOPTION, steps.get(1).advisor());
    }

    @Test
    void shouldCapPlanAtMaxSteps() {

        List<PlanStep> steps = OrchestrationPlanner.parse(
                "MATCHING: a\nCARE: b\nADOPTION: c\nMATCHING: d");

        assertEquals(OrchestrationPlanner.MAX_STEPS, steps.size());
    }

    @Test
    void shouldWrapPlannerFailureAsAiServiceException() {

        when(chatClient.prompt()
                .system(anyString())
                .user("hello")
                .call()
                .content())
                .thenThrow(new RuntimeException("api-key=secret123"));

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> planner.plan("hello"));

        assertFalse(ex.getMessage().contains("secret123"));
    }
}
