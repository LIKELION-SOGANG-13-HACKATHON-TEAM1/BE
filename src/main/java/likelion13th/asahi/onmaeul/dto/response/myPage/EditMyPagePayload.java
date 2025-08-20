package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditMyPagePayload {
    private Long   user_id;
    private String user_name;         // 이름 (닉네임 분리 안할거니까 그냥 이름과 동일)
    private String birth_date;        // "YYYY-MM-DD" (문자열로 반환)
    private String user_phonenumber;  // 읽기전용
    private String user_introduce;
    private String profile_url;       // 프로필 이미지 URL
}
