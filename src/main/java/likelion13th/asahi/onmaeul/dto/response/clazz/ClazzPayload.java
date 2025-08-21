package likelion13th.asahi.onmaeul.dto.response.clazz;

import likelion13th.asahi.onmaeul.dto.response.myPage.PagingInfo;
import lombok.Data;

import java.util.List;

@Data
public class ClazzPayload {
    private List<ClazzItem> classes;
    private PagingInfo paging;
}
