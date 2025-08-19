package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber); // 로그인 아이디로 사용
}