package com.petadoption.ai.tool;

public record PetDetailsResult(

        boolean found,

        String message,

        PetSummary pet
) {
}
