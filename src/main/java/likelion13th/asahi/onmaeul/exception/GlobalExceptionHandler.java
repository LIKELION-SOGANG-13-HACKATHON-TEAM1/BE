package likelion13th.asahi.onmaeul.exception;

import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<?> handle404(ChangeSetPersister.NotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.error("E404", "조회 가능한 요청이 없습니다."));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handle401(UnauthorizedException e) {
        return ResponseEntity.status(401)
                .body(ApiResponse.error("E401", "로그인이 필요합니다."));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handle403(ForbiddenException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.error("E403", "권한이 없습니다."));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handle409(ConflictException e) {
        return ResponseEntity.status(409)
                .body(ApiResponse.error("E409", e.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handle400(BadRequestException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.error("E400", e.getMessage()));
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        // 에러 정보를 담을 Map 생성
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage()); // 예외 메시지 ("메인 리스트 조회 중 예상치 못한 서버 오류가 발생했습니다.")

        // 원인(Cause)이 있으면 원인의 메시지와 클래스 이름을 추가
        Throwable cause = ex.getCause();
        if (cause != null) {
            response.put("cause_message", cause.getMessage());
            response.put("cause_type", cause.getClass().getName());
        }

        // 스택 트레이스를 문자열로 변환하여 추가 (가장 중요한 정보)
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        response.put("stackTrace", sw.toString());

        // 500 Internal Server Error 상태 코드와 함께 에러 정보 반환
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
