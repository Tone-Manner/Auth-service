package com.textrefiner.authservice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // Spring 설정(application.yml)을 읽어오기 위해 필요
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Test
    @DisplayName("JWT 토큰이 정상적으로 생성되고 검증되는지 확인한다.")
    void tokenCreateAndValidateTest() {
        // 1. Given (준비)
        String email = "test@example.com";
        String role = "ROLE_USER";

        // 2. When (실행 - 토큰 발급)
        String token = jwtProvider.createAccessToken(email, role);
        System.out.println("=========================================");
        System.out.println("🎉 발급된 JWT 토큰: " + token);
        System.out.println("=========================================");

        // 3. Then (검증)
        // 토큰이 null이 아닌지 확인
        assertThat(token).isNotNull();

        // 토큰이 유효한지 검증
        boolean isValid = jwtProvider.validateToken(token);
        assertThat(isValid).isTrue();

        // 토큰에서 이메일을 잘 꺼내오는지 확인
        String extractedEmail = jwtProvider.getEmailFromToken(token);
        assertThat(extractedEmail).isEqualTo(email);
    }
}