package likelion13th.asahi.onmaeul.dto.response.requestTab;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RequestStatusPayload {
    private String role;
    private String state;
    private List<PendingRequestPayload> pendingRequests;
    private List<AcceptedRequestPayload> acceptedRequests;
}