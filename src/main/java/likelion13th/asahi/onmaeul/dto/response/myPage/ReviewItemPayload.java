package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.*;

@Getter
@Builder
public class ReviewItemPayload {
    private Long id;
    private String content;
    private Double rating;
}
