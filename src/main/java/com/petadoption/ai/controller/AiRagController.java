package com.petadoption.ai.controller;

import com.petadoption.ai.rag.DocumentIngestionService;
import com.petadoption.dto.response.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/rag")
@RequiredArgsConstructor
public class AiRagController {

    private final DocumentIngestionService documentIngestionService;

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping("/ingest")
    public ApiResponseDto<List<DocumentIngestionService.IngestionResult>>
    ingest() {

        return new ApiResponseDto<>(
                true,
                "Ingestion completed",
                documentIngestionService.ingestAll()
        );
    }
}
