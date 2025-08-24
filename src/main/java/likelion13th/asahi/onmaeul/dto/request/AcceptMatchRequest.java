package likelion13th.asahi.onmaeul.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AcceptMatchRequest {
    @JsonProperty("request_id")
    private Long helpRequestId; // 수락할 요청글 ID
}