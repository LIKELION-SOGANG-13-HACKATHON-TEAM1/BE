package likelion13th.asahi.onmaeul.DTO.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private boolean is_success;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .is_success(true)
                .code("S200")
                .message(message)
                .data(data)
                .build();
    }
}
