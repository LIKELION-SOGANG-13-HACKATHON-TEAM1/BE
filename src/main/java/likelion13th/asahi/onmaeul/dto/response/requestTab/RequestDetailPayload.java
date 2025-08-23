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
    private ActionsDto actions;
    private JuniorInfo juniorInfo;
    private SeniorInfo seniorInfo;
}