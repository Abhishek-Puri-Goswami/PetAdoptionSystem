package com.petadoption.ai.tool;

import com.petadoption.entity.Pet;
import com.petadoption.enums.PetStatus;
import com.petadoption.repository.PetRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetSearchToolsTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetSearchTools petSearchTools;

    @Test
    void shouldReturnAvailablePetsMappedToSummaries() {

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Buddy");
        pet.setSpecies("Dog");
        pet.setStatus(PetStatus.AVAILABLE);

        when(petRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(pet));

        List<PetSummary> result =
                petSearchTools.searchAvailablePets("Dog", null);

        assertEquals(1, result.size());
        assertEquals("Buddy", result.get(0).name());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void shouldCapResultsAtTenPets() {

        List<Pet> elevenPets = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> {
                    Pet pet = new Pet();
                    pet.setId((long) i);
                    pet.setName("Pet" + i);
                    pet.setStatus(PetStatus.AVAILABLE);
                    return pet;
                })
                .toList();

        when(petRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(elevenPets);

        List<PetSummary> result =
                petSearchTools.searchAvailablePets(null, null);

        assertEquals(10, result.size());
    }

    @Test
    void shouldReturnFoundDetailsForAvailablePet() {

        Pet pet = new Pet();
        pet.setId(5L);
        pet.setName("Luna");
        pet.setStatus(PetStatus.AVAILABLE);

        when(petRepository.findById(5L))
                .thenReturn(Optional.of(pet));

        PetDetailsResult result = petSearchTools.getPetDetails(5L);

        assertTrue(result.found());
        assertNotNull(result.pet());
        assertEquals("Luna", result.pet().name());
    }

    @Test
    void shouldNotReturnDetailsForAdoptedPet() {

        Pet pet = new Pet();
        pet.setId(6L);
        pet.setName("Rex");
        pet.setStatus(PetStatus.ADOPTED);

        when(petRepository.findById(6L))
                .thenReturn(Optional.of(pet));

        PetDetailsResult result = petSearchTools.getPetDetails(6L);

        assertFalse(result.found());
        assertNull(result.pet());
        assertNotNull(result.message());
    }

    @Test
    void shouldReturnNotFoundForMissingPet() {

        when(petRepository.findById(99L))
                .thenReturn(Optional.empty());

        PetDetailsResult result = petSearchTools.getPetDetails(99L);

        assertFalse(result.found());
        assertNull(result.pet());
    }
}
