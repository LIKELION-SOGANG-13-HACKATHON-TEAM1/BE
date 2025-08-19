package likelion13th.asahi.onmaeul.dto.response.helpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("request_id")
    private Long requestId;

    private String title;
    private String location;

    @JsonProperty("request_time")
    private String requestTime;

    @JsonProperty("created_at")
    private String createdAt;
    private String status;
    private String route;

    @JsonProperty("ui_flags")
    private UiFlags uiFlags;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class UiFlags{
        @JsonProperty("can_accept")
        private boolean canAccept;
    }

}
