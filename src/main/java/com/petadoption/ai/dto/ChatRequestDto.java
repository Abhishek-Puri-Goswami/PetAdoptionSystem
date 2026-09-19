package com.petadoption.ai.dto;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequestDto(

        // Every message becomes paid model input, so cap it.
        @NotBlank(message = "Message is required")
        @Size(max = Rules.CHAT_MESSAGE_MAX,
                message = "Message must be at most 1000 characters")
        String message
) {
}
