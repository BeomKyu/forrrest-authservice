package com.forrrest.authservice.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
}