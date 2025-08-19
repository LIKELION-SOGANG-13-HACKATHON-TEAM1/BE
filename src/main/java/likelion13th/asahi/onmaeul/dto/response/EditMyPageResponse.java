package likelion13th.asahi.onmaeul.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditMyPageResponse {
    private Long   user_id;
    private String user_name;        // 이름 (닉네임 분리 안할 경우 이름과 동일)
    private String user_introduce;   // 한 줄 소개
}
