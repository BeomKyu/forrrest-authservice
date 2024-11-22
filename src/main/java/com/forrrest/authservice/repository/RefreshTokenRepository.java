package com.forrrest.authservice.repository;

import com.forrrest.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {  // ID 타입을 String으로 변경
    Optional<RefreshToken> findByRefreshToken(String token);
    boolean existsByRefreshToken(String token);
}