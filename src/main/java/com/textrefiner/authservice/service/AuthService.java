package com.textrefiner.authservice.service;

import com.textrefiner.authservice.domain.User;
import com.textrefiner.authservice.dto.AuthResponse;
import com.textrefiner.authservice.dto.LoginRequest;
import com.textrefiner.authservice.dto.SignupRequest;
import com.textrefiner.authservice.repository.UserRepository;
import com.textrefiner.authservice.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 1. 회원가입 로직
    @Transactional
    public void signup(SignupRequest request) {
        // 이미 존재하는 이메일인지 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 암호화 후 User 객체 생성 및 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 암호화 적용!
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    // 2. 로그인 로직
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 이메일로 유저 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호 일치 여부 확인 (평문 비밀번호와 DB의 암호화된 비밀번호 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 인증 성공 시 Access Token 과 Refresh Token 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // DB에 리프레시 토큰 저장 (User 엔티티  업데이트)
        user.updateRefreshToken(refreshToken);

        // 클라이언트에게 토큰(2개)과 유저 정보, 권한(role)을 모두 반환
        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole());
    }

    // 3. 토큰 재발급 로직
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        // 1. 리프레시 토큰 유효기간 및 서명 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. 토큰에서 이메일을 꺼내서 DB의 유저 정보 조회
        String email = jwtProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. DB에 저장된 토큰과 클라이언트가 보낸 refreshToken 이 일치하는지 확인
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("토큰이 일치하지 않습니다. 다시 로그인해주세요.");
        }

        // 4. 모든 검증을 통과했다면 새로운 액세스 토큰 발급하여 반환
        return jwtProvider.createAccessToken(user.getEmail(), user.getRole());
    }
}