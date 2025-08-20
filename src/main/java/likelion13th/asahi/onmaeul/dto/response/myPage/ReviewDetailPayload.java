package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewDetailPayload {
    private Long review_id;
    private Double rating_stars;
    private String review_text;
    private HelpRequestInfo help_request;

    @Getter
    @Builder
    public static class HelpRequestInfo {
        private Long request_id;
        private String title;
        private String requester_name;
        private String location;
        private String request_time;
        private String duration_text;
        private String description;
        private List<String> images;
    }
}