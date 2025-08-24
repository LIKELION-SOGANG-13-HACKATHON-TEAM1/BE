package likelion13th.asahi.onmaeul.dto.response.clazz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ClazzItem {
    private Long id;
    private String title;
    private LocalDateTime schedule;
    @JsonProperty("host_name")
    private String hostName;
    private String status;
    private String description;
}