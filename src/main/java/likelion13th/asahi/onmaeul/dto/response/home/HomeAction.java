package likelion13th.asahi.onmaeul.DTO.response.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeAction{
    private String title;
    private String subtitle;
    private String route;
    private String action;
}
