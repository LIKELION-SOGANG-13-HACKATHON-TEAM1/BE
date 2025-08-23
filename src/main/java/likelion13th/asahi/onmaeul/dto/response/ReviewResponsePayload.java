package likelion13th.asahi.onmaeul.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ReviewResponsePayload {
    private Long reviewId;
    private Long matchId;
    private Long targetId;
    private double rating;
    private OffsetDateTime createdAt;
}
