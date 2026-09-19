package com.petadoption.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponseDto {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    // Only for validation errors: field name -> message, so a form can show
    // every problem next to its field. Omitted (null) for other errors.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> fieldErrors;

}