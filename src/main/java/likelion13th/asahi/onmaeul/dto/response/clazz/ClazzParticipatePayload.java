package likelion13th.asahi.onmaeul.dto.response.clazz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClazzParticipatePayload {
    @JsonProperty("class_id")
    private Long classId;
    @JsonProperty("host_name")
    private String hostName;
}
