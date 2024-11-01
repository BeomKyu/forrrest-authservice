package com.forrrest.authservice.dto.request;

import lombok.Data;

@Data
public class SelectProfileRequest {
    private Long userId;
    private String profileName;
    private String audience; // 외부 서비스 식별자
}