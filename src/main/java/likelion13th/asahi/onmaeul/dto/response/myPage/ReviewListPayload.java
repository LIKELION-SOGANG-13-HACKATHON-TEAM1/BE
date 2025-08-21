package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class ReviewListPayload {
    private List<ReviewItemPayload> review_list;
    private PagingInfo paging;
}
