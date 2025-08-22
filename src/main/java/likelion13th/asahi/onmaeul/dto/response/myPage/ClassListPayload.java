package likelion13th.asahi.onmaeul.dto.response.myPage;

import likelion13th.asahi.onmaeul.domain.ClassStatus;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ClassListPayload {
    private ClassStatus filter_status;
    private List<ClassItemPayload> class_list;
    private PagingInfo paging;
}