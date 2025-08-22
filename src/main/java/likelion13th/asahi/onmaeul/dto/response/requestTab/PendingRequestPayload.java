package likelion13th.asahi.onmaeul.dto.response.requestTab;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingRequestPayload { // '수락 전' 상태의 요청 정보
    private Long requestId;
    private String title;
    private String location;
    private String requestTime;
    private String status; // pending
    private ActionsDto actions;
}
