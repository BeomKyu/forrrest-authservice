package com.forrrest.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileRequest {

    @NotBlank(message = "프로필 이름은 필수입니다.")
    @Size(min = 2, max = 20, message = "프로필 이름은 2자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "프로필 이름은 한글, 영문, 숫자만 사용할 수 있습니다.")
    private String name;
} 