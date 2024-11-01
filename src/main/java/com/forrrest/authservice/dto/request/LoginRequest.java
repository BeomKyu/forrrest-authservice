package com.forrrest.authservice.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}