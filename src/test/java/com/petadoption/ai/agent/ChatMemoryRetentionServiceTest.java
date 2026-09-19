package com.petadoption.ai.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMemoryRetentionServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private AiConversationIds conversationIds;

    @InjectMocks
    private ChatMemoryRetentionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "retentionDays", 30L);
    }

    @Test
    void shouldPurgeConversationsInactiveLongerThanRetention() {

        when(jdbcTemplate.update(anyString(), org.mockito.ArgumentMatchers
                .any(Timestamp.class))).thenReturn(6);

        Instant before = Instant.now().minus(30, ChronoUnit.DAYS);

        int purged = service.purgeInactiveConversations();

        Instant after = Instant.now().minus(30, ChronoUnit.DAYS);

        assertEquals(6, purged);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Timestamp> cutoff =
                ArgumentCaptor.forClass(Timestamp.class);
        verify(jdbcTemplate).update(sql.capture(), cutoff.capture());

        // deletes whole conversations by their LAST message, not rows
        assertTrue(sql.getValue().contains("MAX("));
        assertTrue(sql.getValue().contains("GROUP BY conversation_id"));

        assertFalse(cutoff.getValue().toInstant().isBefore(before));
        assertFalse(cutoff.getValue().toInstant().isAfter(after));
    }

    @Test
    void shouldClearEveryAdvisorConversationOfTheCurrentUserOnly() {

        when(conversationIds.forCurrentUser(AiConversationIds.PET_MATCHING))
                .thenReturn("pet-matching:4");
        when(conversationIds.forCurrentUser(AiConversationIds.ADOPTION))
                .thenReturn("adoption:4");

        service.clearMyHistory();

        verify(chatMemory).clear("pet-matching:4");
        verify(chatMemory).clear("adoption:4");
        verifyNoMoreInteractions(chatMemory);
    }
}
