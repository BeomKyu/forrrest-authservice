package com.forrrest.authservice.dto.request;

import lombok.Data;

@Data
public class AddProfileRequest {
    private Long userId;
    private String profileName;
}