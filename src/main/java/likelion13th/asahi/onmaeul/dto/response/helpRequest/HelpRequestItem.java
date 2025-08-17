package likelion13th.asahi.onmaeul.DTO.response.helpRequest;

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

    //entity를 DTO로 바꿔주는 method
    public static HelpRequestItem fromEntity(HelpRequest e) {
        //String format 입력
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String statusStr = (e.getStatus() != null) ? e.getStatus().toString() : null;
        boolean canAccept = (statusStr != null) && statusStr.equalsIgnoreCase("PENDING");

        return HelpRequestItem.builder()
                .requestId(e.getId())
                .title(e.getTitle())
                .location(e.getLocation())
                .requestTime(e.getRequestTime().format(fmt))         // String으로 변환
                .createdAt(e.getCreatedAt().format(fmt))             // String으로 변환
                .status(statusStr)
                .route("/help-requests/" + e.getId())
                .uiFlags(new UiFlags(canAccept))
                .build();
    }
}
