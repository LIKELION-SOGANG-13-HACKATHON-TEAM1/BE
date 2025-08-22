package likelion13th.asahi.onmaeul.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {
    @Value("${openai.api.key}")
    private String openaikey;

    @Bean
    public OpenAiService openAiService(){
        // API 키를 사용하여 OpenAiService 객체를 생성하고 Spring Bean으로 등록
        return new OpenAiService(openaikey);
    }
}
