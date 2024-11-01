package com.forrrest.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String message;

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
        this.message = "토큰 발급 성공";
    }
}