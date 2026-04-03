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
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
    public AuthResponse login(LoginRequest request) {
        // 이메일로 유저 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호 일치 여부 확인 (평문 비밀번호와 DB의 암호화된 비밀번호 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 인증 성공 시 JWT 토큰 발급
        String token = jwtProvider.createAccessToken(user.getEmail(), user.getRole());

        // 클라이언트에게 토큰과 유저 정보를 반환
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }
}