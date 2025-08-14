package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="Banner")
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
//@setter가 없고 create method로만 Banner 생성 가능
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → Class
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class clazz;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", updatable = false, insertable=false)
    private LocalDateTime createdAt;

    //@setter 대신 외부에서 생성시에는 create method 사용
    @Builder
    public static Banner create(Class clazz, String imageUrl,
                                LocalDateTime startAt, LocalDateTime endAt,
                                boolean isActive){
        return Banner.builder()
                .clazz(clazz)
                .imageUrl(imageUrl)
                .startAt(startAt)
                .endAt(endAt)
                .isActive(isActive)
                .build();
    }
}

