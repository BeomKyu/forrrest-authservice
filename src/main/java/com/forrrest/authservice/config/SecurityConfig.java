package com.forrrest.authservice.config;


import com.forrrest.authservice.security.JwtAuthenticationFilter;
import com.forrrest.authservice.security.JwtProvider;
import com.forrrest.authservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserService userService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화
            .csrf(csrf -> csrf.disable())
            // 세션 비활성화 (STATELESS)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 권한 설정
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .anyRequest().authenticated()
            )
        // CORS 설정 제거
        ;

        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}