package com.textrefiner.authservice.repository;

import com.textrefiner.authservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 유저 정보 찾기 (로그인 시 사용)
    Optional<User> findByEmail(String email);

    // 이메일 중복 가입 확인용 (회원가입 시 사용)
    boolean existsByEmail(String email);
}