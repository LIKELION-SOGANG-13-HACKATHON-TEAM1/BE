package likelion13th.asahi.onmaeul;

import likelion13th.asahi.onmaeul.config.props.GuestHomeProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GuestHomeProps.class)
public class OnmaeulApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnmaeulApplication.class, args);
	}

}
