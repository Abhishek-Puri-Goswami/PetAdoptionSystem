package com.petadoption.ai.tool;

import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.User;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdoptionTools {

    private static final int MAX_SNIPPETS = 3;

    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;

    @Tool(
            name = "getMyApplicationStatus",
            description = "Get the status of the current authenticated "
                    + "user's own adoption applications. Takes no "
                    + "arguments and never accepts a user or "
                    + "application ID - it always reports only the "
                    + "caller's own applications.")
    public List<ApplicationStatusSummary> getMyApplicationStatus() {

        User currentUser = currentUser();

        return adoptionApplicationRepository
                .findByAdopterId(currentUser.getId())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Tool(
            name = "getAdoptionRequirements",
            description = "Look up the platform's real adoption process, "
                    + "fees, and requirements documents for a given "
                    + "topic. Returns the actual relevant document "
                    + "snippets - only answer using what this returns.")
    public List<RequirementSnippet> getAdoptionRequirements(

            @ToolParam(description = "What the user wants to know about, "
                    + "e.g. 'adoption fees' or 'application process'")
            String topic) {

        return vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(topic)
                                .topK(MAX_SNIPPETS)
                                .similarityThreshold(0.5)
                                .build())
                .stream()
                .map(document -> new RequirementSnippet(
                        String.valueOf(
                                document.getMetadata().get("source")),
                        document.getText()))
                .toList();
    }

    private User currentUser() {

        String email = SecurityUtil.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private ApplicationStatusSummary toSummary(
            AdoptionApplication application) {

        return new ApplicationStatusSummary(
                application.getPet().getName(),
                application.getStatus().name(),
                application.getReviewNotes(),
                application.getRejectionReason());
    }
}
