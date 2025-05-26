package com.forrrest.authservice.utils.cookies;

import org.springframework.http.ResponseCookie;

public class CookieUtils {

    public static final String COOKIE_NAME = "forrrestUserRefreshToken";
    private static final String PATH = "/auth";
    private static final boolean HTTP_ONLY = true;
    private static final boolean SECURE = true;
    private static final String SAME_SITE = "Strict";

    private CookieUtils() { /* Util 클래스이므로 인스턴스화 금지 */ }

    public static ResponseCookie createRefreshTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .sameSite(SAME_SITE)
                .path(PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }

    public static ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .sameSite(SAME_SITE)
                .path(PATH)
                .maxAge(0)
                .build();
    }
}
