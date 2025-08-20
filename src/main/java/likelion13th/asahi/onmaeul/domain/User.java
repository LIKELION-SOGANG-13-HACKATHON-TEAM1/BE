package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
@Table(name = "Users") // user는 PostgreSQL에서 예약어라서, 테이블명으로 쓰면 종종 SQL 문법 에러: Users로 바꿈
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    /* 휴대폰 번호 */
    @Column(name = "phone_number", nullable = false, length = 20, unique = true)
    private String phoneNumber;

    /* 비밀번호 해시 (원문 저장 금지, 예: BCrypt) */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /* 프로필 이미지 URL */
    @Column(name = "profile_url", length = 255)
    private String profileUrl;

    /* 평점(0.0~5.0, 소수 1자리 권장) */
    @Column(name = "rating",precision=2)
    private Double rating;

    /* 매칭 카드에 노출되는 한 줄 소개 */
    @Column(name = "introduce", length = 100)
    private String introduce;

    private String birth;


    /* 사용자 역할 */
    @Enumerated(EnumType.STRING) // enum -> 문자열로 저장!
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;
}
