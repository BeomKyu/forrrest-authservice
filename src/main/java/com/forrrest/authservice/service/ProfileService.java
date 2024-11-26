package com.forrrest.authservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forrrest.authservice.dto.request.ProfileRequest;
import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.dto.response.ProfileResponse;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.authservice.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final TokenService tokenService;

    @Transactional
    public Profile createDefaultProfile(User user) {
        return profileRepository.save(Profile.builder()
            .user(user)
            .name("Default Profile")
            .isDefault(true)
            .build());
    }

    public Profile getDefaultProfile(User user) {
        return profileRepository.findAllByUser(user).stream()
            .filter(Profile::isDefault)
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));
    }

    @Transactional
    public ProfileResponse createProfile(String email, ProfileRequest request) {
        User user = userService.getUserByEmail(email);

        if (profileRepository.existsByNameAndUser(request.getName(), user)) {
            throw new CustomException(ErrorCode.PROFILE_DUPLICATION);
        }

        Profile profile = profileRepository.save(Profile.builder()
            .user(user)
            .name(request.getName())
            .isDefault(false)
            .build());

        return ProfileResponse.from(profile);
    }

    public List<ProfileResponse> getProfiles(String email) {
        User user = userService.getUserByEmail(email);
        return profileRepository.findAllByUser(user).stream()
            .map(ProfileResponse::from)
            .collect(Collectors.toList());
    }

    public ProfileResponse getProfile(String email, Long profileId) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileRepository.findByIdAndUser(profileId, user)
            .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));
        return ProfileResponse.from(profile);
    }

    @Transactional
    public void deleteProfile(String email, Long profileId) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileRepository.findByIdAndUser(profileId, user)
            .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        if (profile.isDefault()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        profileRepository.delete(profile);
    }

    @Transactional
    public AuthResponse selectProfile(String email, Long profileId) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileRepository.findByIdAndUser(profileId, user)
            .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        return tokenService.createAuthResponse(user, profile);
    }


}