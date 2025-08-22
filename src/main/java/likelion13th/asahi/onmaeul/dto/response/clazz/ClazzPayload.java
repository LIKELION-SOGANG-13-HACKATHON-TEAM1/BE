package likelion13th.asahi.onmaeul.dto.response.clazz;

import likelion13th.asahi.onmaeul.dto.response.myPage.PagingInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClazzPayload {
    private List<ClazzItem> classes;
    private PagingInfo paging;
}
