package likelion13th.asahi.onmaeul.dto.response.helpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class HelpRequestArticlePayload {
    //상세 요청글용 dto

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

    @JsonProperty("category_id")
    private Long categoryId;
    @JsonProperty("category_name")
    private String categoryName;

    private String description;

    private List<String> images;

   /* @JsonProperty("location_detail")
    private String locationDetail;
    HelpRequest 단계에서 굳이 json 반환 필요 없을듯
    */

    @JsonProperty("ui_flags")
    private UiFlags uiFlags;

    @JsonProperty("estimated_time_in_minutes")
    private Integer estimatedTimeInMinutes;

    private Routes routes;
    private Writer writer;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class UiFlags {
        @JsonProperty("can_accept")
        private boolean canAccept;
        @JsonProperty("can_edit")
        private boolean canEdit;
        @JsonProperty("can_cancel")
        private boolean canCancel;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class Routes {
        private String edit;
        private String cancel;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class Writer {
        @JsonProperty("user_id")
        private Long userId;
        private String name;
        private String role;
    }
}