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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvisorCoordinatorServiceTest {

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

    private void classifierReturns(String message, String label) {
        when(chatClient.prompt()
                .system(anyString())
                .user(message)
                .call()
                .content())
                .thenReturn(label);
    }

    @Test
    void shouldRouteMatchingToMatchingAdvisorOnly() {

        classifierReturns("find me a calm dog", "MATCHING");
        when(petMatchingAdvisorService.ask("find me a calm dog"))
                .thenReturn("Luna!");

        assertEquals("Luna!", coordinator.ask("find me a calm dog"));

        verifyNoInteractions(petCareAdvisorService, adoptionAdvisorService);
    }

    @Test
    void shouldRouteCareToCareAdvisorOnly() {

        classifierReturns("how much exercise?", "CARE");
        when(petCareAdvisorService.ask("how much exercise?"))
                .thenReturn("60 minutes");

        assertEquals("60 minutes", coordinator.ask("how much exercise?"));

        verifyNoInteractions(
                petMatchingAdvisorService, adoptionAdvisorService);
    }

    @Test
    void shouldRouteAdoptionToAdoptionAdvisorOnly() {

        classifierReturns("what are the fees?", "ADOPTION");
        when(adoptionAdvisorService.ask("what are the fees?"))
                .thenReturn("$50-$300");

        assertEquals("$50-$300", coordinator.ask("what are the fees?"));

        verifyNoInteractions(
                petMatchingAdvisorService, petCareAdvisorService);
    }

    @Test
    void shouldAskClarifyingQuestionWhenUnsure() {

        classifierReturns("help", "UNSURE");

        assertEquals(
                AdvisorCoordinatorService.CLARIFYING_QUESTION,
                coordinator.ask("help"));

        verifyNoInteractions(
                petMatchingAdvisorService,
                petCareAdvisorService,
                adoptionAdvisorService);
    }

    @Test
    void shouldTreatGarbageClassifierOutputAsUnsure() {

        classifierReturns("hi", "Sure! I think it is MATCHING.");

        assertEquals(
                AdvisorCoordinatorService.CLARIFYING_QUESTION,
                coordinator.ask("hi"));

        verify(petMatchingAdvisorService, never()).ask(anyString());
    }

    @Test
    void shouldWrapClassifierFailureAsAiServiceException() {

        when(chatClient.prompt()
                .system(anyString())
                .user("hello")
                .call()
                .content())
                .thenThrow(new RuntimeException("api-key=secret123"));

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> coordinator.ask("hello"));

        assertFalse(ex.getMessage().contains("secret123"));
    }
}
