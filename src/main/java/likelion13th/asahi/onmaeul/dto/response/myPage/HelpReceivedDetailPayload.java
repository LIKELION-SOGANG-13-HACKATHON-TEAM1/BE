package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/* 어르신용 VIEW */
public class HelpReceivedDetailPayload {
    private Long   match_id;

    private String title;             // 제목
    private String requester_name;    // 요청자 이름 ("000 어르신")
    private String location;          // 장소
    private String request_time;      // 요청 일시 (문자열, 화면 포맷)
    private String duration_text;     // 소요시간 ("0시간 30분")
    private String description;       // 요청사항 본문

    private List<String> images;      // 첨부 이미지 URL들
}
