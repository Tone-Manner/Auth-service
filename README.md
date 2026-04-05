# 🛡️ Auth Service (인증/인가 마이크로서비스)

Spring Boot와 Spring Security, JWT를 활용하여 구현한 독립적인 인증 서버입니다.

## 🚀 기술 스택
- **Language:** Java 17
- **Framework:** Spring Boot 3.x, Spring Security
- **Database:** MySQL, Spring Data JPA
- **Auth:** JWT (JSON Web Token)

## ✨ 핵심 기능
- **회원가입:** 이메일 중복 검사 및 BCrypt 암호화를 통한 안전한 비밀번호 저장
- **로그인 (JWT):** Access Token 및 Refresh Token 발급
- **토큰 재발급:** 만료된 Access Token을 Refresh Token을 통해 안전하게 재발급
- **보안 설정:** Spring Security 필터 체인을 통한 Stateless (무상태) 인증 로직 구현
- **글로벌 예외 처리:** `@RestControllerAdvice`를 활용한 일관된 에러 응답(JSON) 제공

## 🌐 API 명세 (Swagger)
서버 실행 후 아래 주소에서 API 명세를 확인할 수 있습니다.
- `http://localhost:8081/swagger-ui/index.html`