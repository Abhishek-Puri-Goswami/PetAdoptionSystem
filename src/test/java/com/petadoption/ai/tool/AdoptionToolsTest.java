package com.petadoption.ai.tool;

import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.enums.ApplicationStatus;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptionToolsTest {

    @Mock
    private AdoptionApplicationRepository adoptionApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private AdoptionTools adoptionTools;

    @Test
    void shouldReturnOnlyCurrentUsersApplications() {

        User user = new User();
        user.setId(4L);
        user.setEmail("adopter@test.com");

        Pet pet = new Pet();
        pet.setName("Luna");

        AdoptionApplication application = new AdoptionApplication();
        application.setPet(pet);
        application.setStatus(ApplicationStatus.PENDING);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("adopter@test.com");

            when(userRepository.findByEmail("adopter@test.com"))
                    .thenReturn(Optional.of(user));

            when(adoptionApplicationRepository.findByAdopterId(4L))
                    .thenReturn(List.of(application));

            List<ApplicationStatusSummary> result =
                    adoptionTools.getMyApplicationStatus();

            assertEquals(1, result.size());
            assertEquals("Luna", result.get(0).petName());
            assertEquals("PENDING", result.get(0).status());
        }
    }

    @Test
    void shouldThrowWhenCurrentUserNotFound() {

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("ghost@test.com");

            when(userRepository.findByEmail("ghost@test.com"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> adoptionTools.getMyApplicationStatus());
        }
    }

    @Test
    void shouldReturnRequirementSnippetsFromVectorStore() {

        Document document = new Document(
                "Adoption fees are $50 for cats.",
                Map.of("source", "fees.md"));

        when(vectorStore.similaritySearch(
                        any(SearchRequest.class)))
                .thenReturn(List.of(document));

        List<RequirementSnippet> result =
                adoptionTools.getAdoptionRequirements("fees");

        assertEquals(1, result.size());
        assertEquals("fees.md", result.get(0).source());
        assertEquals(
                "Adoption fees are $50 for cats.",
                result.get(0).content());
    }

    @Test
    void shouldReturnEmptyListWhenNoRequirementsFound() {

        when(vectorStore.similaritySearch(
                        any(SearchRequest.class)))
                .thenReturn(List.of());

        List<RequirementSnippet> result =
                adoptionTools.getAdoptionRequirements(
                        "something unrelated");

        assertTrue(result.isEmpty());
    }
}
