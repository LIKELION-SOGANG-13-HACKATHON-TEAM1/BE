package likelion13th.asahi.onmaeul.dto.response.myPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPagePayload {
    private Long user_id;
    private String user_phonenumber;
    private String user_introduce;
    private String user_name;

    // 유저 프로필 넣어야함
}
