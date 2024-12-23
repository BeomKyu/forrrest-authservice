package com.forrrest.authservice.repository;

import com.forrrest.authservice.entity.Profile;
import com.forrrest.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findAllByUser(User user);
    Optional<Profile> findByIdAndUser(Long id, User user);
    boolean existsByNameAndUser(String name, User user);
} 