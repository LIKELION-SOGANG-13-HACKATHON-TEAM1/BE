package likelion13th.asahi.onmaeul.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DraftResponsePayload {
    private CollectedDataDto collected; // 수집된 데이터
    private List<String> missingFields;
    private boolean canFinish;
}
