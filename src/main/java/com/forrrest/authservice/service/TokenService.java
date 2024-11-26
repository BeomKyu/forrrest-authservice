package com.forrrest.authservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.dto.response.ProfileResponse;
import com.forrrest.authservice.dto.response.TokenInfo;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.RefreshToken;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.repository.RefreshTokenRepository;
import com.forrrest.common.security.config.TokenProperties;
import com.forrrest.common.security.token.JwtTokenProvider;
import com.forrrest.common.security.token.TokenType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenProperties tokenProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse createAuthResponse(User user, Profile profile) {
        Map<String, Object> userClaims = Map.of(
            "username", user.getUsername(),
            "roles", List.of("USER")
        );

        Map<String, Object> profileClaims = Map.of(
            "username", user.getUsername(),
            "roles", List.of("PROFILE")
        );

        String userAccessToken = jwtTokenProvider.createToken(user.getEmail(), TokenType.USER_ACCESS, userClaims);
        String userRefreshToken = jwtTokenProvider.createToken(user.getEmail(), TokenType.USER_REFRESH, userClaims);
        String profileAccessToken = jwtTokenProvider.createToken(
            String.valueOf(profile.getId()),
            TokenType.PROFILE_ACCESS,
            profileClaims
        );
        String profileRefreshToken = jwtTokenProvider.createToken(
            String.valueOf(profile.getId()),
            TokenType.PROFILE_REFRESH,
            profileClaims
        );

        refreshTokenRepository.save(
            new RefreshToken(user.getEmail(), userRefreshToken, LocalDateTime.now()));

        return AuthResponse.builder()
            .userToken(createTokenInfo(userAccessToken, userRefreshToken, TokenType.USER_ACCESS))
            .profileToken(createTokenInfo(profileAccessToken, profileRefreshToken, TokenType.PROFILE_ACCESS))
            .profileResponse(ProfileResponse.from(profile))
            .build();
    }

    private TokenInfo createTokenInfo(String accessToken, String refreshToken, TokenType tokenType) {
        return TokenInfo.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(tokenProperties.getValidity().get(tokenType))
            .build();
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public String getEmailFromToken(String token) {
        return jwtTokenProvider.getAuthentication(token).getName();
    }


}
