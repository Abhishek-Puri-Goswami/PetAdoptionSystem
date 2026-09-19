package com.petadoption.dto.request;

import com.petadoption.validation.Rules;
import jakarta.validation.constraints.*;

public record AdoptionDecisionRequestDto(

        @Size(max = Rules.NOTES_MAX,
                message = "Notes must be at most 500 characters")
        String notes
) {
}
