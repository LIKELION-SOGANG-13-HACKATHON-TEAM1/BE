package likelion13th.asahi.onmaeul.dto.response.requestTab;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RequestDetailPayload {
    private Long requestId;
    private String title;
    private String description;
    private List<String> images;
    private String location;
    private String requestTime;
    private String status;
    private ActionsDto actions; // 청년은 어떤 액션을 취할 수 있는 버튼 없음. (조회만 가능): 어르신용
    private JuniorInfo juniorInfo;
    private SeniorInfo seniorInfo;
    private String category;
}