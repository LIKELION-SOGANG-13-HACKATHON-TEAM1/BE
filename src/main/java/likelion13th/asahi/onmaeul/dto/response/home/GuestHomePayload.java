package likelion13th.asahi.onmaeul.DTO.response.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestHomePayload implements HomePayload {
    private String role;
    private HomeAction guestAction;
}
