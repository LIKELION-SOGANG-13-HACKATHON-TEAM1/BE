package likelion13th.asahi.onmaeul.dto.request;

import lombok.Getter;

@Getter
public class ReviewRequest {
    private Long matchId;
    private Long targetId;
    private double rating;
    private String content;
}
