package likelion13th.asahi.onmaeul.dto.response.requestTab;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestStartPayload {
    private Long match_id;
    private Long request_id;
    private String status;       // "in_progress"
    private String started_at;   // ISO-8601 문자열
}
