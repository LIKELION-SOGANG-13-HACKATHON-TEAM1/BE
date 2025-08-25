package likelion13th.asahi.onmaeul.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("원문 비밀번호: " + rawPassword);
        System.out.println("생성된 해시값: " + encodedPassword);
    }
}
