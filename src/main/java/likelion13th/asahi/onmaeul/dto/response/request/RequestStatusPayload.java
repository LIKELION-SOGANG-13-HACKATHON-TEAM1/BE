package likelion13th.asahi.onmaeul.dto.response.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestStatusPayload {
    private String role;
    private String state;
    private List<RequestDto> requests;
    private List<AcceptedRequestDto> acceptedRequests;
}