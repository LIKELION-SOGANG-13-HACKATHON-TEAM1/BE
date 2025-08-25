package likelion13th.asahi.onmaeul.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class ChatRequest {
    @JsonProperty("session_id")
    private String sessionId; //session_id를 채팅 메시지를 보낼 때마다 서버에 다시 전달 -> 어떤 대화세션 메시지인지 구분
    private String message; // 사용자가 입력한 메시지
    private List<Attachment> attachments;
    private Meta meta;

    @Getter @Setter
    public static class Attachment {
        private String url; // 이미지 url 의미!!
    }

    @Getter @Setter
    public static class Meta {
        @JsonProperty("client_ts")
        private OffsetDateTime clientTs; // 클라이언트에서 메시지가 생성된 시간
    }
}

