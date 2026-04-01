package com.textrefiner.authservice.config;

import com.textrefiner.authservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 지원을 가능하게 함
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (우리는 세션 대신 JWT를 사용하므로 CSRF 보호가 필요 없음)
                .csrf(csrf -> csrf.disable())

                // 2. 세션 비활성화 (서버에 상태를 저장하지 않는 Stateless 설정, MSA의 핵심)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. API URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 회원가입과 로그인 API는 토큰 없이 누구나 접근할 수 있도록 허용(permitAll)
                        .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login").permitAll()
                        // 그 외의 모든 요청은 반드시 인증(토큰)이 필요함
                        .anyRequest().authenticated()
                )

                // 4. 우리가 만든 커스텀 필터를 기본 로그인 검사 필터보다 먼저 실행되도록 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}