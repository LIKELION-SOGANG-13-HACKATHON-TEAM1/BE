package likelion13th.asahi.onmaeul.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false) // 허용되지 않은 필드 오면 400 유도
public class EditMyPageRequest {
    private String name;
    private String introduce;
}
