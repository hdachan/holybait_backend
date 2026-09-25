package com.holyhabit.holyhabit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.holyhabit.holyhabit.repository.UserRepository;
import com.holyhabit.holyhabit.security.JwtFilter;
import com.holyhabit.holyhabit.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/app/version",   // 로그인 전 스플래시에서 호출
                                "/error"          // 예외 발생 시 실제 에러가 403으로 가려지지 않도록
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtFilter(jwtProvider, userRepository, objectMapper),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}