package com.petadoption.ai.tool;

import com.petadoption.dto.request.PetSearchCriteria;
import com.petadoption.entity.Pet;
import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.PetStatus;
import com.petadoption.enums.Temperament;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.PetSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PetSearchTools {

    private static final int MAX_RESULTS = 10;

    private final PetRepository petRepository;

    @Tool(
            name = "searchAvailablePets",
            description = "Search pets that are currently available for "
                    + "adoption. Filters are optional - omit any you don't "
                    + "need. Always returns only AVAILABLE pets, regardless "
                    + "of what is asked.")
    public List<PetSummary> searchAvailablePets(

            @ToolParam(required = false,
                    description = "Species to filter by, e.g. Dog or Cat")
            String species,

            @ToolParam(required = false,
                    description = "Energy level to filter by: LOW, "
                            + "MEDIUM, or HIGH")
            EnergyLevel energyLevel,

            @ToolParam(required = false,
                    description = "Temperament to filter by: CALM, "
                            + "PLAYFUL, INDEPENDENT, AFFECTIONATE, or "
                            + "PROTECTIVE")
            Temperament temperament,

            @ToolParam(required = false,
                    description = "Free-text search over breed, name, "
                            + "or description")
            String search) {

        PetSearchCriteria criteria = new PetSearchCriteria(
                species, energyLevel, temperament, null, null,
                PetStatus.AVAILABLE, search);

        return petRepository
                .findAll(PetSpecification.fromCriteria(criteria))
                .stream()
                .limit(MAX_RESULTS)
                .map(this::toSummary)
                .toList();
    }

    @Tool(
            name = "getPetDetails",
            description = "Get full details for one pet by its ID. Only "
                    + "returns a result if that pet is currently AVAILABLE "
                    + "for adoption.")
    public PetDetailsResult getPetDetails(

            @ToolParam(description = "The pet's ID")
            Long petId) {

        return petRepository.findById(petId)
                .filter(pet -> pet.getStatus() == PetStatus.AVAILABLE)
                .map(pet -> new PetDetailsResult(
                        true, null, toSummary(pet)))
                .orElseGet(() -> new PetDetailsResult(
                        false,
                        "No available pet found with that ID.",
                        null));
    }

    private PetSummary toSummary(Pet pet) {

        return new PetSummary(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getAge(),
                pet.getGender(),
                pet.getEnergyLevel(),
                pet.getTemperament(),
                pet.getDescription());
    }
}
