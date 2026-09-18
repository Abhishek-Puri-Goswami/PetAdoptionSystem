package com.petadoption.ai.agent;

import com.petadoption.entity.User;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.repository.UserRepository;
import com.petadoption.util.SecurityUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiConversationIdsTest {

    // spring_ai_chat_memory.conversation_id is VARCHAR(36)
    private static final int MAX_COLUMN_LENGTH = 36;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AiConversationIds conversationIds;

    @Test
    void shouldBuildKeyFromUserIdNotEmail() {

        User user = new User();
        user.setId(4L);

        try (MockedStatic<SecurityUtil> mocked =
                     Mockito.mockStatic(SecurityUtil.class)) {

            mocked.when(SecurityUtil::getCurrentUserEmail)
                    .thenReturn("a-very-long-email-address-that-would"
                            + "-never-fit@example.com");

            when(userRepository.findByEmail(
                    "a-very-long-email-address-that-would"
                            + "-never-fit@example.com"))
                    .thenReturn(Optional.of(user));

            String key = conversationIds.forCurrentUser("pet-matching:");

            assertEquals("pet-matching:4", key);
            assertFalse(key.contains("@"));
            assertTrue(key.length() <= MAX_COLUMN_LENGTH);
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

            assertThrows(ResourceNotFoundException.class,
                    () -> conversationIds.forCurrentUser("adoption:"));
        }
    }
}
