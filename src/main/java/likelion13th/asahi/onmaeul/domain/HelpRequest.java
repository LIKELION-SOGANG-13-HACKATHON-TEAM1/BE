package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "help_request")
public class HelpRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HelpRequestStatus status;

    @Column(length = 100)
    private String location;

    @Column(name = "location_detail", length = 100)   // 매칭 후에만 보여지는 민감정보
    private String locationDetail;

    @Column(name = "phone_number", length = 20)       // 매칭 후에만 보여지는 민감정보
    private String phoneNumber;

    @Column(name = "request_time")
    private java.time.OffsetDateTime requestTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "help_request_images", joinColumns = @JoinColumn(name = "helprequest_id"))
    @Column(name = "url", nullable = false, length = 255)
    @OrderColumn(name = "sort_index") // 순서 보존
    private java.util.List<String> images = new java.util.ArrayList<>();


    // createdAt → 항상 현재 시간 자동 저장
    //status → 기본값 자동 세팅 (PENDING)
    @PrePersist
    void onCreate() {
        this.createdAt = java.time.OffsetDateTime.now();
        if (this.status == null) this.status = HelpRequestStatus.PENDING;
    }


}

