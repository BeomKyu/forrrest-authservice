package com.forrrest.authservice.service;

import com.forrrest.authservice.dto.request.TokenRequest;
import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.dto.response.ProfileResponse;
import com.forrrest.authservice.dto.response.TokenInfo;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.RefreshToken;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.dto.request.LoginRequest;
import com.forrrest.authservice.dto.request.SignupRequest;
import com.forrrest.authservice.dto.response.UserResponse;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.authservice.repository.RefreshTokenRepository;
import com.forrrest.common.security.config.TokenProperties;
import com.forrrest.common.security.token.JwtTokenProvider;
import com.forrrest.common.security.token.TokenType;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final ProfileService profileService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATION);
        }

        User user = userService.createUser(request);
        profileService.createDefaultProfile(user);

        return UserResponse.from(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        Profile defaultProfile = profileService.getDefaultProfile(user);
        return tokenService.createAuthResponse(user, defaultProfile);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRequest request, Long profileId) {
        if (!tokenService.validateToken(request.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (!refreshTokenRepository.existsByRefreshToken(request.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String email = tokenService.getEmailFromToken(request.getRefreshToken());
        if (profileId == null) {
            User user = userService.getUserByEmail(email);
            Profile defaultProfile = profileService.getDefaultProfile(user);
            return tokenService.createAuthResponse(user, defaultProfile);
        }
        else {
            return profileService.selectProfile(email, profileId);
        }
        // User user = userService.getUserByEmail(email);
        // Profile 
        // Profile defaultProfile = profileService.getDefaultProfile(user);
        
        // return tokenService.createAuthResponse(user, defaultProfile);
    }
}