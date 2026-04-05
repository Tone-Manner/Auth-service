package com.textrefiner.authservice.controller;

import com.textrefiner.authservice.dto.AuthResponse;
import com.textrefiner.authservice.dto.LoginRequest;
import com.textrefiner.authservice.dto.SignupRequest;
import com.textrefiner.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth") // 공통 URL 주소
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입 API (POST /api/v1/auth/signup)
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    // 로그인 API (POST /api/v1/auth/login)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 토큰 재발급 API (POST /api/v1/auth/refresh)
    @PostMapping("/refresh")
    public ResponseEntity<java.util.Map<String, String>> refresh(@RequestBody java.util.Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newAccessToken = authService.refreshAccessToken(refreshToken);

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("accessToken", newAccessToken);

        return ResponseEntity.ok(response);
    }
}