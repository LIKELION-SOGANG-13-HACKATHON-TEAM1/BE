package likelion13th.asahi.onmaeul.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DraftRequest {
    @JsonProperty("session_id")
    private String sessionId;

    private String title;
    private String description;
    private String location;

    @JsonProperty("location_detail")
    private String locationDetail;

    @JsonProperty("request_time")
    private String requestTime;

    private List<String> images;

    @JsonProperty("category_id")
    private Long categoryId;

}
