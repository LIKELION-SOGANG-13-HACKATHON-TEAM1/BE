package likelion13th.asahi.onmaeul.dto.response.helpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class UpdatePayload {
    //상세 요청 페이지 수정용 dto
    @JsonProperty("request_id")
    private final Long requestId;

    private String status;

    @JsonProperty("updated_at")
    private final OffsetDateTime updatedAt;

    private String route;
}
