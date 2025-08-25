package likelion13th.asahi.onmaeul.dto.response.requestTab;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCompletePayload {
    @JsonProperty("match_id") private Long matchId;
    @JsonProperty("request_id") private Long requestId;
    private HelpRequestStatus status; // "completed_unreviewed"
}
