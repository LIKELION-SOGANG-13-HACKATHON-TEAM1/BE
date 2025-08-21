package likelion13th.asahi.onmaeul.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class OpenAiConfig {
    @Value("${openai.api.key}")
    private String openaikey;

    @Bean
    public OpenAiService openAiService(){
        return new OpenAiService(openaikey);
    }
}
