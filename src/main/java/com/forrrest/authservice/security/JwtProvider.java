package com.forrrest.authservice.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.expiration}")
    private Long jwtExpirationInMs;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    private RSAPublicKey externalServicePublicKey; // 외부 서비스의 공개 키를 로드

    @PostConstruct
    public void loadPublicKey() throws Exception {
        // 외부 서비스의 공개 키를 로드
        // 키스토어 생성이 나중으로 미뤄졌으므로, 현재는 키로드를 생략하거나 기본 공개 키를 사용
        // 추후 필요 시 공개 키 파일을 추가하고 로드 로직 구현

        // 예시: external_service_1_public.pem 파일에서 공개 키 로드
        InputStream is = getClass().getClassLoader().getResourceAsStream("keystore/external_service_1_public.pem");
        if (is == null) {
            throw new IllegalArgumentException("Public key file not found");
        }
        String publicKeyContent = new String(is.readAllBytes())
                .replaceAll("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");

        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        this.externalServicePublicKey = (RSAPublicKey) kf.generatePublic(keySpec);
    }

    public String generateEncryptedToken(String username, String profileId, String audience) throws JOSEException {
        // JWT Claims 설정
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .audience(audience) // 외부 서비스 식별자
                .issuer(jwtIssuer)
                .expirationTime(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .claim("profileId", profileId)
                .build();

        // JWE 헤더 설정
        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM)
                .contentType("JWT") // 내부에 JWT가 있음
                .build();

        // JWT를 JWE 객체로 변환
        JWEObject jweObject = new JWEObject(header, new Payload(claimsSet.toJSONObject()));
        jweObject.encrypt(new RSAEncrypter(externalServicePublicKey));

        return jweObject.serialize();
    }

    // JWT Claims에서 사용자 이름 추출
    public String getUsernameFromJWT(String token) throws ParseException, JOSEException {
        JWEObject jweObject = JWEObject.parse(token);
        // 외부 서비스의 개인 키로 복호화 필요 (추후 구현)
        // 현재는 단순히 청크를 추출
        JWTClaimsSet claims = JWTClaimsSet.parse(jweObject.getPayload().toJSONObject());
        return claims.getSubject();
    }

    // JWT Claims에서 프로필 ID 추출
    public String getProfileIdFromJWT(String token) throws ParseException, JOSEException {
        JWEObject jweObject = JWEObject.parse(token);
        // 외부 서비스의 개인 키로 복호화 필요 (추후 구현)
        JWTClaimsSet claims = JWTClaimsSet.parse(jweObject.getPayload().toJSONObject());
        return claims.getStringClaim("profileId");
    }

    // JWT 토큰 검증 메서드 추가 (필요 시 구현)
    public boolean validateEncryptedToken(String token, String expectedAudience) {
        try {
            JWEObject jweObject = JWEObject.parse(token);
            // 외부 서비스의 개인 키로 복호화 필요 (추후 구현)

            // JWT Claims 추출
            JWTClaimsSet claims = JWTClaimsSet.parse(jweObject.getPayload().toJSONObject());

            // `aud` 클레임 검증
            if (!claims.getAudience().contains(expectedAudience)) {
                return false;
            }

            // 만료 시간 검증
            Date expirationTime = claims.getExpirationTime();
            return new Date().before(expirationTime);
        } catch (Exception e) {
            return false;
        }
    }
}