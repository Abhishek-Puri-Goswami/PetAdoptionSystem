package com.petadoption.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {

    private Long userId;

    private String email;

    private String token;

}