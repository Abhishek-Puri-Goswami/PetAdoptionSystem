package com.petadoption.ai.advisor;

import com.petadoption.exception.AiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PetCareAdvisorService {

    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

    public PetCareAdvisorService(
            ChatClient chatClient,
            VectorStore vectorStore) {

        this.chatClient = chatClient;

        this.questionAnswerAdvisor = QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(
                        SearchRequest.builder()
                                .topK(4)
                                .similarityThreshold(0.5)
                                .build())
                .build();
    }

    public String ask(String question) {

        try {

            return chatClient.prompt()
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
