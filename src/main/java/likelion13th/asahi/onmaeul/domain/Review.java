package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="Review")
@Getter //@setter 설정 안함으로 외부 생성 막기
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    //FK->User(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    //FK->Match(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    //전체 자리수 2개, 소숫점 자리 1개(0.0~5.0)
    @Column(nullable=false,precision=2)
    private Double rating;

    @Column(columnDefinition="TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;

    /*외부 new 금지 -> create method로만 Review 만들기 가능
    생성 시 규칙 준수 여부 체크 가능*/
    public static Review create(User writer, Match match, Double rating, String content) {
        Review review = new Review();
        review.writer = writer;
        review.match = match;
        review.setRating(rating); // 검증 포함
        review.content = content;
        return review;
    }

    //rating 검증 method
    private void setRating(double rating) {
        if (rating < 0.0 || rating > 5.0) {
            throw new IllegalArgumentException("rating must be between 0.0 and 5.0");
        }
        this.rating = Math.round(rating * 10.0) / 10.0;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = java.time.OffsetDateTime.now();
    }
}
