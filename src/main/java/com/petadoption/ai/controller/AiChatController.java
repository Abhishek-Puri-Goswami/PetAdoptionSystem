package com.petadoption.ai.controller;

import com.petadoption.ai.advisor.AdoptionAdvisorService;
import com.petadoption.ai.advisor.PetCareAdvisorService;
import com.petadoption.ai.advisor.PetMatchingAdvisorService;
import com.petadoption.ai.dto.ChatRequestDto;
import com.petadoption.ai.dto.ChatResponseDto;
import com.petadoption.dto.response.ApiResponseDto;
import com.petadoption.exception.AiServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final ChatClient chatClient;
    private final PetCareAdvisorService petCareAdvisorService;
    private final PetMatchingAdvisorService petMatchingAdvisorService;
    private final AdoptionAdvisorService adoptionAdvisorService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/ping")
    public ApiResponseDto<ChatResponseDto> ping(
            @Valid @RequestBody ChatRequestDto request) {

        String reply;

        try {

            reply = chatClient.prompt()
                    .user(request.message())
                    .call()
                    .content();

        } catch (Exception ex) {

            log.error("AI chat call failed", ex);

            throw new AiServiceException(
                    "AI service is currently unavailable. "
                            + "Please try again later.");
        }

        return new ApiResponseDto<>(
                true,
                "AI responded successfully",
                new ChatResponseDto(reply)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chat/match")
    public ApiResponseDto<ChatResponseDto> match(
            @Valid @RequestBody ChatRequestDto request) {

        String reply = petMatchingAdvisorService.ask(request.message());

        return new ApiResponseDto<>(
                true,
                "AI responded successfully",
                new ChatResponseDto(reply)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chat/adoption")
    public ApiResponseDto<ChatResponseDto> adoption(
            @Valid @RequestBody ChatRequestDto request) {

        String reply = adoptionAdvisorService.ask(request.message());

        return new ApiResponseDto<>(
                true,
                "AI responded successfully",
                new ChatResponseDto(reply)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chat/care")
    public ApiResponseDto<ChatResponseDto> care(
            @Valid @RequestBody ChatRequestDto request) {

        String reply = petCareAdvisorService.ask(request.message());

        return new ApiResponseDto<>(
                true,
                "AI responded successfully",
                new ChatResponseDto(reply)
        );
    }
}
