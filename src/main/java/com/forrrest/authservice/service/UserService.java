package com.forrrest.authservice.service;

import com.forrrest.authservice.dto.request.SignupRequest;
import com.forrrest.authservice.entity.User;
import com.forrrest.authservice.dto.response.UserResponse;
import com.forrrest.authservice.exception.CustomException;
import com.forrrest.authservice.exception.ErrorCode;
import com.forrrest.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User createUser(SignupRequest request) {
        return userRepository.save(User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .username(request.getUsername())
            .build());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public UserResponse getUserInfo(String email) {
        User user = getUserByEmail(email);
        return UserResponse.from(user);
    }
}