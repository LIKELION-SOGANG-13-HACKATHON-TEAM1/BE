package likelion13th.asahi.onmaeul.dto.response.clazz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClazzArticlePayload {
    @JsonProperty("image_url")
    private String imageUrl;
    private String title;
    private String description;
    @JsonProperty("time_table")
    private List<String> timeTable;
    @JsonProperty("host_name")
    private String hostName;
    @JsonProperty("created_at")
    private String createdAt;
    private String status;
    private LocalDateTime schedule;
}
