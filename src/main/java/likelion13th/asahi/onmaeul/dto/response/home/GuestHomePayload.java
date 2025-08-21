package likelion13th.asahi.onmaeul.dto.response.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestHomePayload implements HomePayload {
    //guest 홈 접근용 dto
    private String role;
    private HomeAction guestAction;
}
