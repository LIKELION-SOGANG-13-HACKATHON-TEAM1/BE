package likelion13th.asahi.onmaeul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion13th.asahi.onmaeul.domain.Category;
import lombok.Data;

import java.util.List;

@Data
public class ChatDraft {
    //Redis에 저장될 임시 객체(dto라 생각해도 별 문제 없을 듯)
    private String sessionId;
    @JsonProperty("category_id")
    private Integer categoryId;
    private String title;
    private String description;
    private String location;
    private String locationDetail;
    private String requestTime;
    private List<String> images;
}
