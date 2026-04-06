package com.textrefiner.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j // 로그 출력을 위한 어노테이션
@Component // 스프링이 이 클래스를 관리하도록 Bean 등록
public class JwtProvider {

    private final Key key;
    private final long expiration;

    // application.yml 에 적어둔 secret 과 expiration 값을 가져와서 세팅
    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long expiration
    ) {
        // 시크릿 키가 Base64 인코딩 여부를 확인 후 처리
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    // 1. Access Token 생성
    public String createAccessToken(String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.expiration);

        return Jwts.builder()
                .setSubject(email) // Payload 에 이메일 저장
                .claim("role", role) // 권한 정보도 같이 저장
                .setIssuedAt(now) // 토큰 발행 시간
                .setExpiration(validity) // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 아까 만든 Key로 HS256 암호화 서명
                .compact(); // 토큰을 문자열로 압축해서 반환
    }

    // 2. 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // 3. 토큰에서 이메일(Subject) 꺼내기
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String createRefreshToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000L); // 7일

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}