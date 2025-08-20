package likelion13th.asahi.onmaeul.dto.request;

import likelion13th.asahi.onmaeul.domain.Category;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRequest {
    private Category category;
    private String title;
    private String description;
    private String location;
    private String locationDetail;
    private List<String> images;
}
