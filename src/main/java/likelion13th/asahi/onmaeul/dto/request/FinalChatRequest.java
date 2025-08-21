package likelion13th.asahi.onmaeul.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FinalChatRequest {
    @JsonProperty("session_id")
    private String sessionId;
}
