package com.forrrest.authservice.controller;

import com.forrrest.authservice.dto.request.*;
import com.forrrest.authservice.dto.response.*;
import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.repository.ProfileRepository;
import com.forrrest.authservice.repository.UserRepository;
import com.forrrest.authservice.security.JwtProvider;
import com.forrrest.authservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    // 생성자 주입
    public AuthController(AuthenticationManager authenticationManager,
                          JwtProvider jwtProvider,
                          UserService userService,
                          ProfileRepository profileRepository,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.userService = userService;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsername(loginRequest.getUsername()).get();
            Set<Profile> profiles = user.getProfiles();

            return ResponseEntity.ok(new LoginResponse("로그인 성공", profiles));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("잘못된 로그인 정보");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 존재하는 사용자입니다.");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword()) // 암호화는 서비스에서 처리
                .build();

        userService.registerUser(user);

        // 기본 프로필 생성
        Profile defaultProfile = Profile.builder()
                .profileName("기본 프로필")
                .user(user)
                .build();

        profileRepository.save(defaultProfile);

        return ResponseEntity.ok("사용자 등록 성공");
    }

    @PostMapping("/select-profile")
    public ResponseEntity<?> selectProfile(@RequestBody SelectProfileRequest request) {
        // 프로필 검증
        Optional<Profile> profileOpt = profileRepository.findByProfileNameAndUserId(
                request.getProfileName(),
                request.getUserId()
        );

        if (profileOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 프로필");
        }

        Profile profile = profileOpt.get();
        String audience = request.getAudience(); // 외부 서비스 식별자

        try {
            String encryptedToken = jwtProvider.generateEncryptedToken(
                    profile.getUser().getUsername(),
                    profile.getId().toString(),
                    audience
            );

            return ResponseEntity.ok(new TokenResponse(encryptedToken));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("토큰 발급 실패");
        }
    }

    @PostMapping("/add-profile")
    public ResponseEntity<?> addProfile(@RequestBody AddProfileRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 사용자");
        }

        User user = userOpt.get();
        Profile profile = Profile.builder()
                .profileName(request.getProfileName())
                .user(user)
                .build();

        profileRepository.save(profile);
        return ResponseEntity.ok(new AddProfileResponse("프로필 추가 성공"));
    }

    @DeleteMapping("/delete-profile")
    public ResponseEntity<?> deleteProfile(@RequestBody DeleteProfileRequest request) {
        Optional<Profile> profileOpt = profileRepository.findById(request.getProfileId());
        if (profileOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 프로필");
        }

        Profile profile = profileOpt.get();
        // 기본 프로필 삭제 방지 로직 추가
        if (profile.getProfileName().equals("기본 프로필")) {
            return ResponseEntity.badRequest().body("기본 프로필은 삭제할 수 없습니다.");
        }

        profileRepository.delete(profile);
        return ResponseEntity.ok("프로필 삭제 성공");
    }
}