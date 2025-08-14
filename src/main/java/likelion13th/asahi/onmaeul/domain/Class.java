package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="Class")
@Getter//@setter 설정 안함으로 외부 생성 막기
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/*외부 new 금지 -> create method로만 Review 만들기 가능
  생성 시 규칙 준수 여부 체크 가능*/
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime schedule;

    @Column(nullable = false, length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", updatable = false, insertable=false)
    private LocalDateTime createdAt;

    @Builder
    public static Class create(Long hostId,
                               String title,
                               String description,
                               LocalDateTime schedule,
                               String location,
                               Status status) {
        return Class.builder()
                .hostId(hostId)
                .title(title)
                .description(description)
                .schedule(schedule)
                .location(location)
                .status(status)
                .build();
    }

    //status 기본값 주기
    @PrePersist
    protected void onCreate() {
        if (this.status == null) this.status = Status.OPEN;
    }

    public enum Status {
        OPEN,
        CLOSED
    }
}