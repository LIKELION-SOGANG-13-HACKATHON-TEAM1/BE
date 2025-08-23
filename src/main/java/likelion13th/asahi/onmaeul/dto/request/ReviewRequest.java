package likelion13th.asahi.onmaeul.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ReviewRequest {
    @JsonProperty("match_id")
    private Long matchId;
    @JsonProperty("target_id")
    private Long targetId;
    private double rating;
    private String content;
}
