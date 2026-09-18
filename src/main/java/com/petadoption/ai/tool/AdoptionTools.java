package com.petadoption.ai.tool;

import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.User;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdoptionTools {

    private static final int MAX_SNIPPETS = 3;

    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;

    // Chosen from measured scores (ai-eval/rag_scores.py): off-topic
    // questions peak ~0.24, real in-scope ones start ~0.38.
    @Value("${app.ai.rag.similarity-threshold:0.30}")
    private double similarityThreshold;

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

            @ToolParam(description = "The user's question as one complete "
                    + "natural-language sentence, in their own words, "
                    + "e.g. 'Can I pay for my adoption through the "
                    + "platform?'. Do NOT pass bare keywords like "
                    + "'payment methods' - short keyword queries "
                    + "retrieve far worse (measured).")
            String topic) {

        List<RequirementSnippet> snippets = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(topic)
                                .topK(MAX_SNIPPETS)
                                .similarityThreshold(similarityThreshold)
                                .build())
                .stream()
                .map(document -> new RequirementSnippet(
                        String.valueOf(
                                document.getMetadata().get("source")),
                        document.getText()))
                .toList();

        // The model chooses `topic` itself, so recall problems usually
        // start here: log what it asked for and what came back.
        log.debug("getAdoptionRequirements topic='{}' -> {} snippet(s) {}",
                topic, snippets.size(),
                snippets.stream().map(RequirementSnippet::source).toList());

        return snippets;
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
