package com.forrrest.authservice.controller;

import com.forrrest.authservice.dto.request.ProfileRequest;
import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.dto.response.ProfileResponse;
import com.forrrest.authservice.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Profile", description = "프로필 API")
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "프로필 생성", description = "새로운 프로필을 생성합니다.")
    @SecurityRequirement(name = "bearer-token")
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.createProfile(userDetails.getUsername(), request));
    }

    @Operation(summary = "프로필 목록 조회", description = "사용자의 모든 프로필을 조회합니다.")
    @SecurityRequirement(name = "bearer-token")
    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getProfiles(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(profileService.getProfiles(userDetails.getUsername()));
    }

    @Operation(summary = "프로필 상세 조회", description = "특정 프로필의 상세 정보를 조회합니다.")
    @SecurityRequirement(name = "bearer-token")
    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> getProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.getProfile(userDetails.getUsername(), profileId));
    }

    @Operation(summary = "프로필 삭제", description = "특정 프로필을 삭제합니다.")
    @SecurityRequirement(name = "bearer-token")
    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long profileId) {
        profileService.deleteProfile(userDetails.getUsername(), profileId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로필 선택", description = "특정 프로필을 선택합니다.")
    @PatchMapping("/{profileId}/select")
    @SecurityRequirement(name = "bearer-token")
    public ResponseEntity<AuthResponse> selectProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.selectProfile(userDetails.getUsername(), profileId));
    }
}