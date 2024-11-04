package com.forrrest.authservice.service;

import java.time.LocalDateTime;

import com.forrrest.authservice.dto.request.TokenRequest;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.RefreshToken;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.dto.request.LoginRequest;
import com.forrrest.authservice.dto.request.SignupRequest;
import com.forrrest.authservice.dto.response.TokenResponse;
import com.forrrest.authservice.dto.response.UserResponse;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.authservice.repository.ProfileRepository;
import com.forrrest.authservice.repository.RefreshTokenRepository;
import com.forrrest.authservice.repository.UserRepository;
import com.forrrest.authservice.security.JwtProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATION);
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .username(request.getUsername())
            .build();

        user = userRepository.save(user);

        // 기본 프로필 자동 생성
        Profile defaultProfile = Profile.builder()
            .profileName(user.getUsername() + "의 프로필")
            .user(user)
            .build();
        profileRepository.save(defaultProfile);

        return UserResponse.from(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // RefreshToken 만료 시간 계산 (예: 7일)
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

        // RefreshToken 저장 또는 업데이트
        RefreshToken refreshTokenEntity = refreshTokenRepository.findById(user.getEmail())
            .map(entity -> entity.updateToken(refreshToken, expiryDate))
            .orElse(RefreshToken.builder()
                .email(user.getEmail())
                .refreshToken(refreshToken)
                .expiryDate(expiryDate)
                .build());

        refreshTokenRepository.save(refreshTokenEntity);

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(3600)  // accessToken 만료 시간 (초)
            .build();
    }

    @Transactional
    public TokenResponse refreshToken(TokenRequest request) {
        // 리프레시 토큰 유효성 검증
        if (!jwtProvider.validateToken(request.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // DB에서 리프레시 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(request.getRefreshToken())
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        // 토큰 만료 여부 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        // 새로운 액세스 토큰 발급
        String newAccessToken = jwtProvider.createAccessToken(refreshToken.getEmail());

        // 리프레시 토큰 재발급 (선택사항 - 리프레시 토큰 재사용 방지)
        String newRefreshToken = jwtProvider.createRefreshToken(refreshToken.getEmail());
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

        // 리프레시 토큰 업데이트
        refreshToken.updateToken(newRefreshToken, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(3600)
            .build();
    }
} 