package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}
