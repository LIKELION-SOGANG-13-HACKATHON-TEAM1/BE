package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpItemPayload {
    private Long match_id;       // 매칭 ID
    private String help_title;   // 요청글 제목
    private String matched_at;   // 매칭된 시간 (ISO-8601 문자열)
    private String location;     // 도움 장소
}
