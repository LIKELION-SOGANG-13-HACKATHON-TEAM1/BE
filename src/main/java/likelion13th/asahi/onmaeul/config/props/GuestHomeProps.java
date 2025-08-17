package likelion13th.asahi.onmaeul.config.props;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix="home.guest")
public record GuestHomeProps(
        @NotBlank String title,
        @NotBlank String subtitle,
        @NotBlank String route,
        @NotBlank String action,
        @NotBlank String message
) {//guest의 경우 하드 코딩이 아닌 설정(yaml)에서 가져온다)
     }
