package likelion13th.asahi.onmaeul.dto.response.clazz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

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
    @JsonProperty("host_id")
    private Long hostId;
    @JsonProperty("created_at")
    private String createdAt;
    private String status;
    private String schedule;
}
