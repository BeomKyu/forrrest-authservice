package com.forrrest.authservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.authservice.repository.ProfileRepository;
import com.forrrest.common.security.config.TokenProperties;
import com.forrrest.common.security.token.JwtTokenProvider;
import com.forrrest.common.security.token.TokenType;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private UserService userService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenProperties tokenProperties;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void selectProfile_WithValidProfile_ShouldReturnAuthResponse() {
        // given
        String email = "test@test.com";
        Long profileId = 1L;
        User user = User.builder()
            .email(email)
            .username("Test username")
            .build();
        Profile profile = Profile.builder()
            .id(profileId)
            .user(user)
            .name("Test Profile")
            .build();

        Map<String, Object> userClaims = Map.of(
            "username", user.getUsername(),
            "roles", List.of("USER")
        );

        Map<String, Object> profileClaims = Map.of(
            "username", user.getUsername(),
            "roles", List.of("PROFILE")
        );

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(profileRepository.findByIdAndUser(profileId, user)).thenReturn(Optional.of(profile));
        when(jwtTokenProvider.createToken(email, TokenType.USER_ACCESS, userClaims)).thenReturn("userAccessToken");
        when(jwtTokenProvider.createToken(email, TokenType.USER_REFRESH, userClaims)).thenReturn("userRefreshToken");
        when(jwtTokenProvider.createToken(String.valueOf(profileId), TokenType.PROFILE_ACCESS, profileClaims)).thenReturn("profileAccessToken");
        when(jwtTokenProvider.createToken(String.valueOf(profileId), TokenType.PROFILE_REFRESH, profileClaims)).thenReturn("profileRefreshToken");
        when(tokenProperties.getValidity()).thenReturn(Map.of(
            TokenType.USER_ACCESS, 3600000L,
            TokenType.PROFILE_ACCESS, 3600000L
        ));

        // when
        AuthResponse response = profileService.selectProfile(email, profileId);

        // then
        assertThat(response.getUserToken().getAccessToken()).isEqualTo("userAccessToken");
        assertThat(response.getUserToken().getRefreshToken()).isEqualTo("userRefreshToken");
        assertThat(response.getProfileToken().getAccessToken()).isEqualTo("profileAccessToken");
        assertThat(response.getProfileToken().getRefreshToken()).isEqualTo("profileRefreshToken");
        assertThat(response.getProfileResponse().getId()).isEqualTo(profile.getId());
    }

    @Test
    void selectProfile_WithInvalidProfile_ShouldThrowException() {
        // given
        String email = "test@test.com";
        Long profileId = 1L;
        User user = User.builder()
            .email(email)
            .build();

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(profileRepository.findByIdAndUser(profileId, user)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.selectProfile(email, profileId))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROFILE_NOT_FOUND);
    }
}
