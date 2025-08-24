package likelion13th.asahi.onmaeul.dto.response.helpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion13th.asahi.onmaeul.domain.Category;
import likelion13th.asahi.onmaeul.domain.HelpRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpRequestItem {
    //요청 메인 화면용 dto

    @JsonProperty("request_id")
    private Long requestId;

    private String title;
    private String location;

    @JsonProperty("request_time")
    private String requestTime;

    @JsonProperty("created_at")
    private String createdAt;
    private String category;
    private String route;

}
