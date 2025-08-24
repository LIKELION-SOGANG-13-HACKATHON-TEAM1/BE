package likelion13th.asahi.onmaeul.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    //응답 전용 dto
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
    /** 에러 응답 */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .is_success(false)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
