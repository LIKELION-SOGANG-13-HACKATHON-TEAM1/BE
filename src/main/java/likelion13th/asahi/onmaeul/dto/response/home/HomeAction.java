package likelion13th.asahi.onmaeul.dto.response.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeAction{
    //senior 전용 home에서 버튼
    private String title;
    private String subtitle;
    private String route;
    private String action;
}
