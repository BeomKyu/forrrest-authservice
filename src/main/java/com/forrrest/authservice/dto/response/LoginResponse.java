package com.forrrest.authservice.dto.response;

import com.forrrest.authservice.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private Set<Profile> profiles;
}