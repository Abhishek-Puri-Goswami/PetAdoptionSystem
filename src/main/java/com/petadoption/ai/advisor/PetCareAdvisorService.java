package com.petadoption.ai.advisor;

import com.petadoption.exception.AiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PetCareAdvisorService {

    private static final String INSTRUCTIONS = """
            You are the pet care advisor for a pet adoption platform.
            Answer ONLY from the context documents supplied with the
            question. If the context is empty or does not contain the
            answer, say briefly that you don't have that information.
            Do NOT answer from general knowledge, and do NOT offer to
            (for example never say "from general knowledge I can say...").
            Keep answers concise.
            """;

    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

    public PetCareAdvisorService(
            ChatClient chatClient,
            VectorStore vectorStore,
            @Value("${app.ai.rag.similarity-threshold:0.30}")
            double similarityThreshold) {

        this.chatClient = chatClient;

        this.questionAnswerAdvisor = QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(
                        SearchRequest.builder()
                                .topK(4)
                                .similarityThreshold(similarityThreshold)
                                .build())
                .build();
    }

    public String ask(String question) {

        try {

            return chatClient.prompt()
                    .system(INSTRUCTIONS)
                    .advisors(questionAnswerAdvisor)
                    .user(question)
                    .call()
                    .content();

        } catch (Exception ex) {

            log.error("Pet care advisor call failed", ex);

            throw new AiServiceException(
                    "AI service is currently unavailable. "
                            + "Please try again later.");
        }
    }
}
