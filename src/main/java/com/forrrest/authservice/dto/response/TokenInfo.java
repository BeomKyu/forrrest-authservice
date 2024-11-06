package com.forrrest.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenInfo {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}