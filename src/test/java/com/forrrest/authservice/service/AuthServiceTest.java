package com.forrrest.authservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import com.forrrest.authservice.dto.request.LoginRequest;
import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.dto.response.ProfileResponse;
import com.forrrest.authservice.dto.response.TokenInfo;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.RefreshToken;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.authservice.repository.ProfileRepository;
import com.forrrest.authservice.repository.RefreshTokenRepository;
import com.forrrest.common.security.config.TokenProperties;
import com.forrrest.common.security.token.JwtTokenProvider;
import com.forrrest.common.security.token.TokenType;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private ProfileService profileService;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password");
        User user = User.builder()
            .email("test@test.com")
            .username("Test username")
            .password("encodedPassword")
            .build();
        Profile defaultProfile = Profile.builder()
            .id(1L)
            .user(user)
            .name("Default")
            .isDefault(true)
            .build();
        AuthResponse expectedResponse = AuthResponse.builder()
            .userToken(new TokenInfo("userAccessToken", "userRefreshToken", "Bearer", 3600000L))
            .profileToken(new TokenInfo("profileAccessToken", "profileRefreshToken", "Bearer", 3600000L))
            .profileResponse(ProfileResponse.from(defaultProfile))
            .build();

        when(userService.getUserByEmail(request.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(profileService.getDefaultProfile(user)).thenReturn(defaultProfile);
        when(tokenService.createAuthResponse(user, defaultProfile)).thenReturn(expectedResponse);

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");
        User user = User.builder()
            .email("test@test.com")
            .password("encodedPassword")
            .build();

        when(userService.getUserByEmail(request.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
    }
}
