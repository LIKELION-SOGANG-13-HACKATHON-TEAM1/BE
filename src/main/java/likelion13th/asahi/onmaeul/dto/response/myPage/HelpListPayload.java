package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpListPayload {
    private List<HelpItemPayload> help_list; // 도움 내역 리스트
    private PagingInfo paging;                // 페이징 메타데이터
}
