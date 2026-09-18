package com.petadoption.ai.advisor;

import com.petadoption.ai.agent.AgentExecutor;
import com.petadoption.ai.agent.AgentResponse;
import com.petadoption.ai.tool.AdoptionTools;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdoptionAdvisorService {

    private static final String CONVERSATION_PREFIX = "adoption:";

    private static final String INSTRUCTIONS = """
            You are an adoption assistant for a pet adoption platform.

            You can help with two things:
            - The status of the CURRENT user's own adoption
              applications, using getMyApplicationStatus. This tool
              takes no arguments and always reports only the
              authenticated caller's own applications. If a user asks
              about someone else's application, or gives you a
              specific application or user ID to look up, explain that
              you can only report the current user's own applications
              and offer to check those instead.
            - Questions about the adoption process, fees, and
              requirements, using getAdoptionRequirements. Only answer
              using the document snippets that tool actually returns -
              if it returns nothing relevant, say you don't have that
              information rather than guessing.

            Never fabricate an application, a status, or a policy
            detail that a tool did not actually return.
            """;

    private final AgentExecutor agentExecutor;
    private final AdoptionTools adoptionTools;

    public String ask(String message) {

        String conversationId =
                CONVERSATION_PREFIX + SecurityUtil.getCurrentUserEmail();

        AgentResponse response = agentExecutor.execute(
                INSTRUCTIONS,
                conversationId,
                message,
                adoptionTools);

        return response.reply();
    }
}
