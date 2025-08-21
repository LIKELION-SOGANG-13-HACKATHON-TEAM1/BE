package likelion13th.asahi.onmaeul.dto.response.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActionsDto {
    private boolean canEdit;
    private boolean canDelete;
}
