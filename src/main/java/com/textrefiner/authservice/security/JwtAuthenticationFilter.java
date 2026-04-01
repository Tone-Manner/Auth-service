package com.textrefiner.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰을 꺼냄
        String token = resolveToken(request);

        // 2. 토큰이 존재하고, 유효한 토큰인지 검사
        if (token != null && jwtProvider.validateToken(token)) {

            // 3. 유효하다면 토큰에서 이메일을 추출
            String email = jwtProvider.getEmailFromToken(token);

            // 4. 스프링 시큐리티에게 "인증된 사용자"라고 명찰을 달아줌 (SecurityContext에 저장)
            // 권한은 임시로 "ROLE_USER"를 부여함 (필요시 토큰에서 role을 꺼내올 수도 있음)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 다음 필터로 요청을 넘김
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 "Bearer "라는 글자를 떼어내고 순수 토큰 문자열만 가져오는 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 헤더에 값이 있고, "Bearer "로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null; // 토큰이 없거나 이상하면 null 반환
    }
}