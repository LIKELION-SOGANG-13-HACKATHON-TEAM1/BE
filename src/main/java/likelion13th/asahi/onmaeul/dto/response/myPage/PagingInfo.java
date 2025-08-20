package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagingInfo {
    private boolean all;        // 전체 조회 여부
    private int count;          // 현재 페이지 아이템 개수
    private boolean has_next;   // 다음 페이지 여부
    private String next_cursor; // 다음 페이지 시작 커서 (없으면 null)
}
