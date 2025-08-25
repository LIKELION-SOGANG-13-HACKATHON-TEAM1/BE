package likelion13th.asahi.onmaeul.dto.response.requestTab;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestCompletePayload {
    @JsonProperty("match_id") private Long matchId;
    @JsonProperty("request_id") private Long requestId;
    private String status; // "completed_unreviewed"
}
