package com.petadoption.ai.agent;

import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.repository.UserRepository;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds the chat-memory key for the CURRENT authenticated user:
 * "<advisor>:<userId>" (e.g. "adoption:4").
 *
 * The id comes from the server-side login, never from the client. The
 * user id (not the email) is used because the memory table's
 * conversation_id column is VARCHAR(36) - an email-based key can exceed
 * it - and so no email address is stored in the memory table.
 */
@Component
@RequiredArgsConstructor
public class AiConversationIds {

    // One prefix per advisor that keeps memory (shared with the advisors
    // and with history erasure so they can never drift apart).
    public static final String PET_MATCHING = "pet-matching:";
    public static final String ADOPTION = "adoption:";
    public static final java.util.List<String> ALL_ADVISORS =
            java.util.List.of(PET_MATCHING, ADOPTION);

    private final UserRepository userRepository;

    public String forCurrentUser(String advisorPrefix) {

        String email = SecurityUtil.getCurrentUserEmail();

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"))
                .getId();

        return advisorPrefix + userId;
    }
}
