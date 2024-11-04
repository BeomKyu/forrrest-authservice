package com.forrrest.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileRequest {

    @NotBlank(message = "프로필 이름은 필수입니다.")
    private String profileName;
} 