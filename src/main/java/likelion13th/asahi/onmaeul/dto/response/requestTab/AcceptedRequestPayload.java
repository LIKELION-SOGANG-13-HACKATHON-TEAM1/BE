package likelion13th.asahi.onmaeul.dto.response.requestTab;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AcceptedRequestPayload { // '수락 완료' 상태의 요청 정보
    private Long requestId;
    private String title;
    private String location;
    private String requestTime;
    private String status; // accepted
    private ActionsDto actions;
    private JuniorInfo juniorInfo;
}