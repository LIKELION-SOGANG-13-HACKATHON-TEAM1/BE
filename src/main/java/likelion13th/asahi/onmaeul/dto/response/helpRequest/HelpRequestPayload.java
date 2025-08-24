package likelion13th.asahi.onmaeul.dto.response.helpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion13th.asahi.onmaeul.dto.response.home.HomePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpRequestPayload{
    //요청 main 화면용 dto
    List<HelpRequestItem> items;
    @JsonProperty("next_cursor")
    String nextCursor;
    @JsonProperty("has_more")
    boolean hasMore;
}
