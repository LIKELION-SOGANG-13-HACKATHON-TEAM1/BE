package likelion13th.asahi.onmaeul.dto.response.requestTab;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActionsDto {
    private boolean canEdit;      // '수정' 버튼 활성화 여부
    private boolean canDelete;    // '취소' 버튼 활성화 여부
    private boolean canStart;     // '도움 시작' 버튼 활성화 여부
    private boolean canComplete;  // '도움 완료' 버튼 활성화 여부
    private boolean canReview;    // '리뷰 남기기' 버튼 활성화 여부
}
