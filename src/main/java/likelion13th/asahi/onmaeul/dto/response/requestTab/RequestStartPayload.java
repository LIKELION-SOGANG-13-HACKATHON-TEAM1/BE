package likelion13th.asahi.onmaeul.dto.response.requestTab;

import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestStartPayload {
    private Long match_id;
    private Long request_id;
    private HelpRequestStatus status;       // "in_progress"
}
