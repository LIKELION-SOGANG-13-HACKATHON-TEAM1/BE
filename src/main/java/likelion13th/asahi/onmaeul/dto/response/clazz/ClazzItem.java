package likelion13th.asahi.onmaeul.dto.response.clazz;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClazzItem {
    private Long id;
    private String title;
    private String schedule;
    @JsonProperty("host_id")
    private Long hostId;
    private String status;
    private String description;
}
