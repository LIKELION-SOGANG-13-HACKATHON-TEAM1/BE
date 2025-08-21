package likelion13th.asahi.onmaeul.dto.response.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeniorHomePayload implements HomePayload {
    //senior 전용 home dto
    private String role;
    private List<HomeAction> homeActions;
}
