package com.petadoption.ai;

import com.petadoption.ai.tool.AdoptionTools;
import com.petadoption.ai.tool.PetSearchTools;

import org.junit.jupiter.api.Test;

import org.springframework.ai.tool.annotation.Tool;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guard for the standing rule "v1 AI tools are read-only": the model
 * may search and explain, never submit/approve/reject/change anything.
 * Fails the build if a write ever appears in the AI layer, so it cannot
 * be added quietly.
 */
class ReadOnlyAiLayerTest {

    private static final Path AI_SOURCES =
            Path.of("src/main/java/com/petadoption/ai");

    // Explicit, reviewed writers: admin-only RAG ingestion, and the chat
    // memory retention job (purges/erases the AI's OWN stored messages,
    // never platform data).
    private static final Set<String> ALLOWED_WRITERS = Set.of(
            "DocumentIngestionService.java",
            "ChatMemoryRetentionService.java");

    private static final Pattern WRITE_CALL = Pattern.compile(
            "\\.(save|saveAll|saveAndFlush|delete\\w*|persist|merge"
                    + "|flush|update|batchUpdate)\\s*\\(");

    private static final Pattern WRITE_TOOL_NAME = Pattern.compile(
            "(?i)^(create|update|delete|remove|approve|reject|submit"
                    + "|cancel|withdraw|set|save|book|send|reset).*");

    @Test
    void aiLayerContainsNoWriteCallsOutsideIngestion() throws IOException {

        List<String> offenders;

        try (Stream<Path> files = Files.walk(AI_SOURCES)) {

            offenders = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !ALLOWED_WRITERS.contains(
                            p.getFileName().toString()))
                    .filter(this::containsWriteCall)
                    .map(p -> p.getFileName().toString())
                    .toList();
        }

        assertEquals(List.of(), offenders,
                "AI layer must stay read-only; write call found in: "
                        + offenders);
    }

    @Test
    void ingestionIsTheOnlyPlaceUsingJdbcOrEntityManager()
            throws IOException {

        try (Stream<Path> files = Files.walk(AI_SOURCES)) {

            List<String> offenders = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !ALLOWED_WRITERS.contains(
                            p.getFileName().toString()))
                    .filter(p -> read(p).contains("JdbcTemplate")
                            || read(p).contains("EntityManager"))
                    .map(p -> p.getFileName().toString())
                    .toList();

            assertEquals(List.of(), offenders);
        }
    }

    @Test
    void noToolMethodIsNamedLikeAWriteAction() {

        Set<Class<?>> toolClasses =
                Set.of(PetSearchTools.class, AdoptionTools.class);

        int toolCount = 0;

        for (Class<?> toolClass : toolClasses) {
            for (Method method : toolClass.getDeclaredMethods()) {

                if (method.isAnnotationPresent(Tool.class)) {

                    toolCount++;

                    assertTrue(
                            !WRITE_TOOL_NAME.matcher(method.getName())
                                    .matches(),
                            "Tool looks like a write action: "
                                    + method.getName());
                }
            }
        }

        // Fails if a new tool class is added without being listed above.
        assertEquals(4, toolCount,
                "Tool count changed - review the new tool for writes "
                        + "and add its class to this test");
    }

    private boolean containsWriteCall(Path path) {
        return WRITE_CALL.matcher(stripComments(read(path))).find();
    }

    private String stripComments(String source) {
        return source
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)//.*$", "");
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
