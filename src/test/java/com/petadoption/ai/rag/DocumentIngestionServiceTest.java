package com.petadoption.ai.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DocumentIngestionService documentIngestionService;

    @Test
    void shouldIngestAllUnseenDocuments() {

        when(jdbcTemplate.queryForObject(
                anyString(), eq(Integer.class),
                anyString(), anyString()))
                .thenReturn(0);

        List<DocumentIngestionService.IngestionResult> results =
                documentIngestionService.ingestAll();

        assertEquals(3, results.size());

        assertTrue(results.stream()
                .allMatch(r -> r.status().equals("INGESTED")));

        assertTrue(results.stream()
                .allMatch(r -> r.chunkCount() > 0));

        verify(jdbcTemplate, times(3))
                .update(anyString(), anyString());

        verify(vectorStore, times(3))
                .add(anyList());
    }

    @Test
    void shouldSkipUnchangedDocuments() {

        when(jdbcTemplate.queryForObject(
                anyString(), eq(Integer.class),
                anyString(), anyString()))
                .thenReturn(1);

        List<DocumentIngestionService.IngestionResult> results =
                documentIngestionService.ingestAll();

        assertEquals(3, results.size());

        assertTrue(results.stream()
                .allMatch(r -> r.status().equals("SKIPPED_UNCHANGED")));

        assertTrue(results.stream()
                .allMatch(r -> r.chunkCount() == 0));

        verify(jdbcTemplate, never())
                .update(anyString(), anyString());

        verify(vectorStore, never())
                .add(anyList());
    }

    @Test
    void shouldTagChunksWithSourceAndHashMetadata() {

        when(jdbcTemplate.queryForObject(
                anyString(), eq(Integer.class),
                anyString(), anyString()))
                .thenReturn(0);

        documentIngestionService.ingestAll();

        org.mockito.ArgumentCaptor<List<Document>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);

        verify(vectorStore, times(3)).add(captor.capture());

        for (List<Document> chunks : captor.getAllValues()) {

            assertFalse(chunks.isEmpty());

            for (Document chunk : chunks) {

                assertNotNull(
                        chunk.getMetadata().get("source"));

                assertNotNull(
                        chunk.getMetadata().get("hash"));
            }
        }
    }
}
