package likelion13th.asahi.onmaeul.dto.response.requestTab;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActionsDto {
    private boolean canEdit;
    private boolean canDelete;
}
