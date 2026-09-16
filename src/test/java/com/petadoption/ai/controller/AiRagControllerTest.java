package com.petadoption.ai.controller;

import com.petadoption.ai.rag.DocumentIngestionService;
import com.petadoption.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiRagController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiRagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentIngestionService documentIngestionService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldTriggerIngestion() throws Exception {

        when(documentIngestionService.ingestAll())
                .thenReturn(List.of(
                        new DocumentIngestionService.IngestionResult(
                                "fees.md", "INGESTED", 3),
                        new DocumentIngestionService.IngestionResult(
                                "adoption-process.md",
                                "SKIPPED_UNCHANGED", 0)
                ));

        mockMvc.perform(post("/api/ai/rag/ingest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.length()")
                        .value(2))
                .andExpect(jsonPath("$.data[0].status")
                        .value("INGESTED"));
    }
}
