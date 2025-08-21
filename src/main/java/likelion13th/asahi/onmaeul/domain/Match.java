package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE) 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    /* 응답자(도와주는 사람): 여러 매칭을 가질 수 있음 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responser_id", nullable = false)
    private User responser;

    /* 매칭된 요청: 1요청당 1매칭(Unique) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "helprequest_id", nullable = false, unique = true)
    private HelpRequest helpRequest;

    // Match에 대한 Review를 참조하는 필드 추가
    @OneToOne(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Review review;

    /* 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchStatus status;

    /* 시간 정보 */
    @Column(name = "matched_at", nullable = false, updatable = false)
    private OffsetDateTime matchedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    // ---------- 라이프사이클 ----------
    @PrePersist
    void onCreate() {
        this.matchedAt = OffsetDateTime.now();
        if (this.status == null) this.status = MatchStatus.ACCEPTED;
    }

}
