package likelion13th.asahi.onmaeul.dto.response.home;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JuniorHomePayload implements HomePayload {
    private String role;
    @JsonProperty("list_endpoint")
    private String listEndpoint;

}
