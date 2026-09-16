package com.petadoption.ai.rag;

import com.petadoption.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private static final String DOC_LOCATION_PATTERN = "classpath:ai-docs/*.md";
    private static final String SOURCE_METADATA_KEY = "source";
    private static final String HASH_METADATA_KEY = "hash";

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    public List<IngestionResult> ingestAll() {

        Resource[] resources;

        try {

            resources = new PathMatchingResourcePatternResolver()
                    .getResources(DOC_LOCATION_PATTERN);

        } catch (IOException ex) {

            log.error("Failed to list RAG documents", ex);

            throw new AiServiceException(
                    "AI service is currently unavailable. "
                            + "Please try again later.");
        }

        List<IngestionResult> results = new ArrayList<>();

        for (Resource resource : resources) {
            results.add(ingestOne(resource));
        }

        return results;
    }

    private IngestionResult ingestOne(Resource resource) {

        String source = resource.getFilename();

        String rawContent;

        try {

            rawContent = resource.getContentAsString(
                    StandardCharsets.UTF_8);

        } catch (IOException ex) {

            log.error("Failed to read RAG document {}", source, ex);

            return new IngestionResult(source, "FAILED", 0);
        }

        String hash = sha256(rawContent);

        Integer existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store "
                        + "WHERE metadata->>'source' = ? "
                        + "AND metadata->>'hash' = ?",
                Integer.class,
                source,
                hash);

        if (existingCount != null && existingCount > 0) {
            return new IngestionResult(source, "SKIPPED_UNCHANGED", 0);
        }

        jdbcTemplate.update(
                "DELETE FROM vector_store WHERE metadata->>'source' = ?",
                source);

        MarkdownDocumentReader reader = new MarkdownDocumentReader(
                resource,
                MarkdownDocumentReaderConfig.builder()
                        .withAdditionalMetadata(
                                SOURCE_METADATA_KEY, source)
                        .withAdditionalMetadata(
                                HASH_METADATA_KEY, hash)
                        .build());

        List<Document> parsed = reader.get();
        List<Document> chunks = textSplitter.apply(parsed);

        vectorStore.add(chunks);

        return new IngestionResult(source, "INGESTED", chunks.size());
    }

    private String sha256(String text) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    text.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "SHA-256 not available", ex);
        }
    }

    public record IngestionResult(
            String source,
            String status,
            int chunkCount
    ) {
    }
}
