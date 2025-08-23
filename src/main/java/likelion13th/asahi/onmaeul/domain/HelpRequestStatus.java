package likelion13th.asahi.onmaeul.domain;

public enum HelpRequestStatus {
    PENDING,   // 대기
    MATCHED,   // 매칭됨
    IN_PROGRESS, // 도움중
    COMPLETED, // 완료
    CANCELED,

    COMPLETED_UNREVIEWED, // 완료되었으나 리뷰가 작성되지 않음
    COMPLETED_REVIEWED    // 완료되었고 리뷰가 작성됨
    ;
}
