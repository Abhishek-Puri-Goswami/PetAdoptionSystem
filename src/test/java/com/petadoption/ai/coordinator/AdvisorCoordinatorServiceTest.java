package com.petadoption.ai.coordinator;

import com.petadoption.ai.advisor.AdoptionAdvisorService;
import com.petadoption.ai.advisor.PetCareAdvisorService;
import com.petadoption.ai.advisor.PetMatchingAdvisorService;
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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvisorCoordinatorServiceTest {

    @Mock
    private OrchestrationPlanner planner;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private PetMatchingAdvisorService petMatchingAdvisorService;

    @Mock
    private PetCareAdvisorService petCareAdvisorService;

    @Mock
    private AdoptionAdvisorService adoptionAdvisorService;

    @InjectMocks
    private AdvisorCoordinatorService coordinator;

    private void mergeReturns(String reply) {
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenReturn(reply);
    }

    @Test
    void shouldAskClarifyingQuestionWhenPlanIsEmpty() {

        when(planner.plan("help")).thenReturn(List.of());

        assertEquals(
                AdvisorCoordinatorService.CLARIFYING_QUESTION,
                coordinator.ask("help"));

        verifyNoInteractions(
                petMatchingAdvisorService,
                petCareAdvisorService,
                adoptionAdvisorService,
                chatClient);
    }

    @Test
    void shouldReturnSingleStepAnswerDirectlyWithoutMerging() {

        when(planner.plan("fees?")).thenReturn(List.of(
                new PlanStep(Route.ADOPTION, "what are the fees")));
        when(adoptionAdvisorService.ask("what are the fees"))
                .thenReturn("$50-$300");

        assertEquals("$50-$300", coordinator.ask("fees?"));

        verifyNoInteractions(
                petMatchingAdvisorService, petCareAdvisorService,
                chatClient);
    }

    @Test
    void shouldPassEarlierAnswerIntoLaterStepAndMerge() {

        when(planner.plan("dog and fee")).thenReturn(List.of(
                new PlanStep(Route.MATCHING, "find a calm dog"),
                new PlanStep(Route.ADOPTION, "fee for that pet")));
        when(petMatchingAdvisorService.ask("find a calm dog"))
                .thenReturn("Luna the Beagle");
        when(adoptionAdvisorService.ask(contains("Luna the Beagle")))
                .thenReturn("Dogs cost $50-$300");
        mergeReturns("Luna fits you; fees are $50-$300.");

        assertEquals("Luna fits you; fees are $50-$300.",
                coordinator.ask("dog and fee"));

        verify(adoptionAdvisorService).ask(contains("reference data"));
    }

    @Test
    void shouldStillAnswerWhenOneStepFails() {

        when(planner.plan("dog and fee")).thenReturn(List.of(
                new PlanStep(Route.MATCHING, "find a calm dog"),
                new PlanStep(Route.ADOPTION, "fee")));
        when(petMatchingAdvisorService.ask("find a calm dog"))
                .thenThrow(new AiServiceException("down"));
        when(adoptionAdvisorService.ask("fee"))
                .thenReturn("$50-$300");
        // only matches if the failed step is passed to the merger
        // marked UNAVAILABLE
        when(chatClient.prompt()
                .system(anyString())
                .user(contains("UNAVAILABLE"))
                .call()
                .content())
                .thenReturn("Fees are $50-$300; I couldn't search pets now.");

        assertEquals("Fees are $50-$300; I couldn't search pets now.",
                coordinator.ask("dog and fee"));
    }

    @Test
    void shouldThrowWhenEveryStepFails() {

        when(planner.plan("dog and fee")).thenReturn(List.of(
                new PlanStep(Route.MATCHING, "a"),
                new PlanStep(Route.ADOPTION, "b")));
        when(petMatchingAdvisorService.ask(anyString()))
                .thenThrow(new AiServiceException("down"));
        when(adoptionAdvisorService.ask(anyString()))
                .thenThrow(new AiServiceException("down"));

        assertThrows(AiServiceException.class,
                () -> coordinator.ask("dog and fee"));
    }

    @Test
    void shouldFallBackToPlainAnswersWhenMergeFails() {

        when(planner.plan("q")).thenReturn(List.of(
                new PlanStep(Route.CARE, "a"),
                new PlanStep(Route.ADOPTION, "b")));
        when(petCareAdvisorService.ask("a")).thenReturn("Walk daily.");
        when(adoptionAdvisorService.ask(contains("Walk daily.")))
                .thenReturn("Fees vary.");
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenThrow(new RuntimeException("merge down"));

        String reply = coordinator.ask("q");

        assertTrue(reply.contains("Walk daily."));
        assertTrue(reply.contains("Fees vary."));
    }
}
