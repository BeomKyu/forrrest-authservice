package com.forrrest.authservice.controller;

import com.forrrest.authservice.dto.request.LoginRequest;
import com.forrrest.authservice.dto.request.SignupRequest;
import com.forrrest.authservice.dto.request.TokenRequest;
import com.forrrest.authservice.dto.response.AuthResponse;
import com.forrrest.authservice.dto.response.UserResponse;
import com.forrrest.authservice.service.AuthService;

import com.forrrest.authservice.utils.cookies.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @Operation(summary = "로그인", description = "사용자 인증 후 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpServletResponse) {
        AuthResponse authResponse = authService.login(request);
        ResponseCookie refreshCookie = CookieUtils.createRefreshTokenCookie(
                authResponse.getUserToken().getRefreshToken(),
                authResponse.getUserToken().getExpiresIn()
        );
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        //todo authResponse 안의 refreshToken 지우기
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "토큰 갱신(쿠키 기반 refreshToken)", description = "헤더에 입력된 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다. Profile ID를 입력하지 않은 경우 default Profile 토큰을 발급합니다.",
            security = {@SecurityRequirement(name = CookieUtils.COOKIE_NAME),
                    @SecurityRequirement(name = "bearer-token")
            })
    @PostMapping("/refresh")
    @SecurityRequirement(name = CookieUtils.COOKIE_NAME)
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody TokenRequest request,
            HttpServletRequest httpServletRequest,
            @RequestParam(required = false) Long profileId,
//            @CookieValue(name = CookieUtils.COOKIE_NAME, required = false) String existingRefresh,
            HttpServletResponse httpServletResponse) {
        Cookie cookie = WebUtils.getCookie(httpServletRequest, CookieUtils.COOKIE_NAME);
        if (cookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse authResponse = authService.refreshToken(request, profileId);
        ResponseCookie refreshCookie = CookieUtils.createRefreshTokenCookie(
                authResponse.getUserToken().getRefreshToken(),
                authResponse.getUserToken().getExpiresIn()
        );
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        //todo authResponse 안의 refreshToken 지우기, 코드 최적화
        return ResponseEntity.ok(authResponse);


    }

    @Operation(summary = "로그아웃(쿠키 삭제)", description = "헤더에 입력된 리프레시 토큰을 사용하여 로그아웃합니다.",
            security = {@SecurityRequirement(name = CookieUtils.COOKIE_NAME),
                    @SecurityRequirement(name = "bearer-token")
            })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // 1) 쿠키 삭제용 ResponseCookie 생성
        ResponseCookie deleteCookie = CookieUtils.deleteRefreshTokenCookie();

        // 2) HTTP 헤더에 추가
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        //todo refreshToken 삭제 코드 추가

        // 3) 204 No Content 로 응답
        return ResponseEntity.noContent().build();
    }
}