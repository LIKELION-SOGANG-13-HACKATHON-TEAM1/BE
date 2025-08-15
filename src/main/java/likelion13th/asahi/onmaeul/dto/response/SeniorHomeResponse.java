package likelion13th.asahi.onmaeul.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SeniorHomeResponse {
    private boolean isSuccess;
    private String code;
    private String message;
    private String role;
    private List<HomeActions> homeActions;

    @Data
    @AllArgsConstructor
    public class HomeActions{
        private String title;
        private String subtitle;
        private String route;
        private String action;
    }
}
