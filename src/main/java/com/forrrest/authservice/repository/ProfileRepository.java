package com.forrrest.authservice.repository;

import com.forrrest.authservice.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByProfileNameAndUserId(String profileName, Long userId);
}