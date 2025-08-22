package likelion13th.asahi.onmaeul.dto.response.requestTab;

import likelion13th.asahi.onmaeul.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeniorInfo {
    private String name;
    private String phoneNumber;
    private String profileImageUrl;

    public static SeniorInfo from(User user) {
        return SeniorInfo.builder()
                .name(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileUrl())
                .build();
    }
}