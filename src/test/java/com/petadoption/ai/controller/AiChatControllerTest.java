package com.petadoption.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.ai.advisor.AdoptionAdvisorService;
import com.petadoption.ai.advisor.PetCareAdvisorService;
import com.petadoption.ai.advisor.PetMatchingAdvisorService;
import com.petadoption.ai.coordinator.AdvisorCoordinatorService;
import com.petadoption.ai.dto.ChatRequestDto;
import com.petadoption.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private PetCareAdvisorService petCareAdvisorService;

    @MockitoBean
    private PetMatchingAdvisorService petMatchingAdvisorService;

    @MockitoBean
    private AdoptionAdvisorService adoptionAdvisorService;

    @MockitoBean
    private AdvisorCoordinatorService advisorCoordinatorService;

    @Test
    void shouldReturnCoordinatorReply() throws Exception {

        ChatRequestDto request = new ChatRequestDto("Find me a dog");

        when(advisorCoordinatorService.ask("Find me a dog"))
                .thenReturn("I found Luna.");

        mockMvc.perform(
                        post("/api/ai/chat")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply")
                        .value("I found Luna."));
    }

    @Test
    void shouldReturnAiReply() throws Exception {

        ChatRequestDto request =
                new ChatRequestDto("What are your hours?");

        when(chatClient.prompt()
                .user("What are your hours?")
                .call()
                .content())
                .thenReturn("We are open 9am to 6pm.");

        mockMvc.perform(
                        post("/api/ai/ping")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.reply")
                        .value("We are open 9am to 6pm."));
    }

    @Test
    void shouldReturn503WhenAiCallFails() throws Exception {

        ChatRequestDto request =
                new ChatRequestDto("Hello");

        when(chatClient.prompt()
                .user("Hello")
                .call()
                .content())
                .thenThrow(new RuntimeException(
                        "connection refused: api-key=secret123"));

        mockMvc.perform(
                        post("/api/ai/ping")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("AI service is currently unavailable. "
                                + "Please try again later."));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {

        ChatRequestDto request = new ChatRequestDto("");

        mockMvc.perform(
                        post("/api/ai/ping")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOverlongMessage() throws Exception {

        ChatRequestDto request =
                new ChatRequestDto("a".repeat(1001));

        mockMvc.perform(
                        post("/api/ai/chat")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnMatchReply() throws Exception {

        ChatRequestDto request =
                new ChatRequestDto("Find me an available dog");

        when(petMatchingAdvisorService.ask("Find me an available dog"))
                .thenReturn("I found Luna, an available Beagle.");

        mockMvc.perform(
                        post("/api/ai/chat/match")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply")
                        .value("I found Luna, an available Beagle."));
    }

    @Test
    void shouldReturnAdoptionReply() throws Exception {

        ChatRequestDto request =
                new ChatRequestDto("What's my application status?");

        when(adoptionAdvisorService.ask(
                "What's my application status?"))
                .thenReturn("Your application for Luna is PENDING.");

        mockMvc.perform(
                        post("/api/ai/chat/adoption")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply")
                        .value("Your application for Luna is PENDING."));
    }

    @Test
    void shouldReturnPetCareReply() throws Exception {

        ChatRequestDto request =
                new ChatRequestDto("How much exercise does a high "
                        + "energy dog need?");

        when(petCareAdvisorService.ask(
                "How much exercise does a high energy dog need?"))
                .thenReturn("At least 60 minutes a day.");

        mockMvc.perform(
                        post("/api/ai/chat/care")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply")
                        .value("At least 60 minutes a day."));
    }
}
