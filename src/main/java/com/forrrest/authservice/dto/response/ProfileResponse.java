package com.forrrest.authservice.dto.response;

import com.forrrest.authservice.entity.Profile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProfileResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProfileResponse from(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .name(profile.getName())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
} 