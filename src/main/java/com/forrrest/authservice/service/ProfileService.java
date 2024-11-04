package com.forrrest.authservice.service;

import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.dto.request.ProfileRequest;
import com.forrrest.authservice.dto.response.ProfileResponse;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.authservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserService userService;

    @Transactional
    public ProfileResponse createProfile(String email, ProfileRequest request) {
        User user = userService.getUser(email);

        if (profileRepository.existsByProfileNameAndUser(request.getProfileName(), user)) {
            throw new CustomException(ErrorCode.PROFILE_DUPLICATION);
        }

        Profile profile = Profile.builder()
                .profileName(request.getProfileName())
                .user(user)
                .build();

        return ProfileResponse.from(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> getProfiles(String email) {
        User user = userService.getUser(email);
        return profileRepository.findAllByUser(user).stream()
                .map(ProfileResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email, Long profileId) {
        User user = userService.getUser(email);
        Profile profile = profileRepository.findByIdAndUser(profileId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));
        return ProfileResponse.from(profile);
    }

    @Transactional
    public void deleteProfile(String email, Long profileId) {
        User user = userService.getUser(email);
        Profile profile = profileRepository.findByIdAndUser(profileId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));
        profileRepository.delete(profile);
    }
} 