package likelion13th.asahi.onmaeul.dto.response.helpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@Builder
public class DeletePayload {
    @JsonProperty("request_id")
    private final Long requestId;

    private String status;

    @JsonProperty("deleted_at")
    private final OffsetDateTime deletedAt;

    private String route;
}
