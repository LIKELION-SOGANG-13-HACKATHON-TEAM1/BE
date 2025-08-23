package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="Class")
@Builder
@Getter//@setter 설정 안함으로 외부 생성 막기
@AllArgsConstructor(access=AccessLevel.PRIVATE)
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

    // 이 매핑을 추가해야만 JPA가 hostId를 기반으로 User 엔티티 객체를 정상적으로 가져올 수 있다!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", insertable = false, updatable = false)
    private User host;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime schedule;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(nullable=false,columnDefinition = "TEXT")
    private String timetable;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassStatus status;

    @Column(name = "created_at", updatable = false,nullable = false)
    private java.time.OffsetDateTime createdAt;


    public static Class create(Long hostId,
                               String title,
                               String description,
                               LocalDateTime schedule,
                               String location,
                               ClassStatus status) {
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
        this.createdAt = java.time.OffsetDateTime.now();
        if (this.status == null) this.status = ClassStatus.OPEN;
    }
}