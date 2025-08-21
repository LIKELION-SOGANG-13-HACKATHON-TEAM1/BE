package likelion13th.asahi.onmaeul.dto.response.request;

import likelion13th.asahi.onmaeul.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JuniorInfo {
    private String name;
    private String phoneNumber;
    private String profileImageUrl;

    public static JuniorInfo from(User user) {
        return JuniorInfo.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
