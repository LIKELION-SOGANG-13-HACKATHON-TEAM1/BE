package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ClassListPayload {
    private String filter_status;
    private List<ClassItemPayload> class_list;
    private PagingInfo paging;
}