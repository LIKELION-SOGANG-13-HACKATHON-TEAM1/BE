package likelion13th.asahi.onmaeul.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponsePayload implements Serializable {
    private String session_id;
    private String bot_reply;
    private CollectedForm collected;
    private List<String> missing_fields;
    private boolean can_finish;
    private Integer session_ttl_seconds;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CollectedForm implements Serializable {
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

// Redis에 객체를 넣을 때 JDK 직렬화를 쓰면 Serializable 필요. 또 Jackson이 타입을 찍어낼 때 기본 생성자 없으면 실패할 수 있다 함..