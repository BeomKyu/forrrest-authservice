package com.forrrest.authservice.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.dto.response.ProfileResponse;
import com.forrrest.authservice.dto.response.TokenInfo;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.authservice.repository.ProfileRepository;
import com.forrrest.common.security.config.TokenProperties;
import com.forrrest.common.security.token.JwtTokenProvider;

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
    @Mock
    private TokenService tokenService;

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
        AuthResponse expectedResponse = AuthResponse.builder()
            .userToken(new TokenInfo("userAccessToken", "userRefreshToken", "Bearer", 3600000L))
            .profileToken(new TokenInfo("profileAccessToken", "profileRefreshToken", "Bearer", 3600000L))
            .profileResponse(ProfileResponse.from(profile))
            .build();

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(profileRepository.findByIdAndUser(profileId, user)).thenReturn(Optional.of(profile));
        when(tokenService.createAuthResponse(user, profile)).thenReturn(expectedResponse);

        // when
        AuthResponse response = profileService.selectProfile(email, profileId);

        // then
        assertThat(response).isEqualTo(expectedResponse);
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
