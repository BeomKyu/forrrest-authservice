package com.forrrest.authservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private TokenInfo userToken;
    private TokenInfo profileToken;
    private ProfileResponse profileResponse;
}