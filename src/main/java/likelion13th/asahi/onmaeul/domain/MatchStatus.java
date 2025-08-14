package likelion13th.asahi.onmaeul.domain;

public enum MatchStatus {
    ACCEPTED,       // 수락됨(매칭 성사)
    IN_PROGRESS,    // 진행 중
    COMPLETED       // 완료
    // 매칭 되면 취소 못함!!
}