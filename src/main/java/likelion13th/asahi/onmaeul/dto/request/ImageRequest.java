package likelion13th.asahi.onmaeul.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageRequest {
    @JsonProperty("file")
    MultipartFile imageFile;
}
