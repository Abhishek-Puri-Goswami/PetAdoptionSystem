package com.petadoption.ai.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Keeps stored AI conversations from living forever (they are personal
 * data): a scheduled purge of inactive conversations, and erasure of the
 * caller's own history on request.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemoryRetentionService {

    private static final String PURGE_SQL =
            "DELETE FROM spring_ai_chat_memory WHERE conversation_id IN ("
                    + "SELECT conversation_id FROM spring_ai_chat_memory "
                    + "GROUP BY conversation_id "
                    + "HAVING MAX(\"timestamp\") < ?)";

    private final JdbcTemplate jdbcTemplate;
    private final ChatMemory chatMemory;
    private final AiConversationIds conversationIds;

    @Value("${app.ai.memory.retention-days:30}")
    private long retentionDays;

    /** Deletes every conversation whose LAST message is older than the cutoff. */
    @Scheduled(cron = "${app.ai.memory.purge-cron:0 0 3 * * *}")
    public int purgeInactiveConversations() {

        Timestamp cutoff = Timestamp.from(
                Instant.now().minus(retentionDays, ChronoUnit.DAYS));

        int rows = jdbcTemplate.update(PURGE_SQL, cutoff);

        if (rows > 0) {
            log.info("Purged {} chat-memory rows older than {} days",
                    rows, retentionDays);
        }

        return rows;
    }

    /** Erases the CURRENT user's stored conversations (all advisors). */
    public void clearMyHistory() {

        for (String advisorPrefix : AiConversationIds.ALL_ADVISORS) {
            chatMemory.clear(
                    conversationIds.forCurrentUser(advisorPrefix));
        }
    }
}
