package likelion13th.asahi.onmaeul.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatResponsePayload {
    private String session_id;
    private String bot_reply;
    private CollectedForm collected;
    private List<String> missing_fields;
    private boolean can_finish;
    private Integer session_ttl_seconds; // 세션이 만료되기까지 남은 시간(초)

    @Getter
    @Builder
    public static class CollectedForm {
        private Integer category_id;
        private String title;
        private String description;
        private String location;
        private String location_detail;
        private String phone_number;
        private String request_time;
        private List<String> images;
    }
}
