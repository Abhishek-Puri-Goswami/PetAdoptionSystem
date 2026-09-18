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
