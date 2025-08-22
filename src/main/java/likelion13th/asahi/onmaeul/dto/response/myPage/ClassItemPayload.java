package likelion13th.asahi.onmaeul.dto.response.myPage;

import likelion13th.asahi.onmaeul.domain.ClassStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassItemPayload {
    private Long id;
    private String title;
    private Long host_id;
    private String host_name; // UI에 보이는 '강사 OOO 청년'을 위해 추가함!
    private String schedule;
    private ClassStatus status;
    private String description;
}
