package com.forrrest.authservice.service;

import com.forrrest.authservice.dto.request.TokenRequest;
import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.dto.response.ProfileResponse;
import com.forrrest.authservice.dto.response.TokenInfo;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.dto.request.LoginRequest;
import com.forrrest.authservice.dto.request.SignupRequest;
import com.forrrest.authservice.dto.response.UserResponse;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.common.security.config.TokenProperties;
import com.forrrest.common.security.token.JwtTokenProvider;
import com.forrrest.common.security.token.TokenType;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenProperties tokenProperties;
    private final PasswordEncoder passwordEncoder;

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

        String userAccessToken = jwtTokenProvider.createToken(user.getEmail(), TokenType.USER_ACCESS);
        String userRefreshToken = jwtTokenProvider.createToken(user.getEmail(), TokenType.USER_REFRESH);
        String profileAccessToken = jwtTokenProvider.createToken(
            String.valueOf(defaultProfile.getId()), 
            TokenType.PROFILE_ACCESS
        );
        String profileRefreshToken = jwtTokenProvider.createToken(
            String.valueOf(defaultProfile.getId()), 
            TokenType.PROFILE_REFRESH
        );

        return AuthResponse.builder()
            .userToken(TokenInfo.builder()
                .accessToken(userAccessToken)
                .refreshToken(userRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProperties.getValidity().get(TokenType.USER_ACCESS))
                .build())
            .profileToken(TokenInfo.builder()
                .accessToken(profileAccessToken)
                .refreshToken(profileRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProperties.getValidity().get(TokenType.PROFILE_ACCESS))
                .build())
            .defaultProfile(ProfileResponse.from(defaultProfile))
            .build();
    }

    @Transactional
    public AuthResponse refreshToken(TokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(request.getRefreshToken());
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        Profile defaultProfile = profileService.getDefaultProfile(user);

        String userAccessToken = jwtTokenProvider.createToken(user.getEmail(), TokenType.USER_ACCESS);
        String userRefreshToken = jwtTokenProvider.createToken(user.getEmail(), TokenType.USER_REFRESH);
        String profileAccessToken = jwtTokenProvider.createToken(
            String.valueOf(defaultProfile.getId()),
            TokenType.PROFILE_ACCESS
        );
        String profileRefreshToken = jwtTokenProvider.createToken(
            String.valueOf(defaultProfile.getId()),
            TokenType.PROFILE_REFRESH
        );

        return AuthResponse.builder()
            .userToken(TokenInfo.builder()
                .accessToken(userAccessToken)
                .refreshToken(userRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProperties.getValidity().get(TokenType.USER_ACCESS))
                .build())
            .profileToken(TokenInfo.builder()
                .accessToken(profileAccessToken)
                .refreshToken(profileRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProperties.getValidity().get(TokenType.PROFILE_ACCESS))
                .build())
            .defaultProfile(ProfileResponse.from(defaultProfile))
            .build();
    }
}