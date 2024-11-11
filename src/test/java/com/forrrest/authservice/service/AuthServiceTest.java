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

import com.forrrest.authservice.dto.request.LoginRequest;
import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
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
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenProperties tokenProperties;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password");
        Long profileId = 1L;
        User user = User.builder()
            .email("test@test.com")
            .password("encodedPassword")
            .build();
        Profile defaultProfile = Profile.builder()
            .id(profileId)
            .user(user)
            .name("Default")
            .isDefault(true)
            .build();

        Map<String, Object> userClaims = Map.of(
            "username", user.getEmail(),
            "roles", List.of("USER")
        );

        Map<String, Object> profileClaims = Map.of(
            "username", user.getEmail(),
            "roles", List.of("PROFILE")
        );

        when(userService.getUserByEmail(request.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(profileService.getDefaultProfile(user)).thenReturn(defaultProfile);
        when(jwtTokenProvider.createToken(user.getEmail(), TokenType.USER_ACCESS, userClaims)).thenReturn("userAccessToken");
        when(jwtTokenProvider.createToken(user.getEmail(), TokenType.USER_REFRESH, userClaims)).thenReturn("userRefreshToken");
        when(jwtTokenProvider.createToken("1", TokenType.PROFILE_ACCESS, profileClaims)).thenReturn("profileAccessToken");
        when(jwtTokenProvider.createToken("1", TokenType.PROFILE_REFRESH, profileClaims)).thenReturn("profileRefreshToken");
        when(tokenProperties.getValidity()).thenReturn(Map.of(
            TokenType.USER_ACCESS, 3600000L,
            TokenType.PROFILE_ACCESS, 3600000L
        ));

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response.getUserToken().getAccessToken()).isEqualTo("userAccessToken");
        assertThat(response.getUserToken().getRefreshToken()).isEqualTo("userRefreshToken");
        assertThat(response.getProfileToken().getAccessToken()).isEqualTo("profileAccessToken");
        assertThat(response.getProfileToken().getRefreshToken()).isEqualTo("profileRefreshToken");
        assertThat(response.getDefaultProfile().getId()).isEqualTo(defaultProfile.getId());
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
