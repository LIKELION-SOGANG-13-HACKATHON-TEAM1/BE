package likelion13th.asahi.onmaeul.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ChatPreparePayload {
    private String session_id;
    private String greeting;
    private List<String> tips;
    private List<String> suggested_chats;
}
