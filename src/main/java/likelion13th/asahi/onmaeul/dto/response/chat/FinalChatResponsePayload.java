package likelion13th.asahi.onmaeul.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FinalChatResponsePayload {
    @JsonProperty("request_id")
    private Long requestId;
    private String title;
    @JsonProperty("category_id")
    private Long categoryId;
    private String description;
    private String location;
    @JsonProperty("location_detail")
    private String locationDetail;
    @JsonProperty("request_time")
    private String requestTime;
    private List<String> images;
    private String status;
    @JsonProperty("created_at")
    private String createdAt;
    private String route;
    @JsonProperty("estimated_time_in_minutes")
    private Integer estimatedTimeInMinutes;
}
