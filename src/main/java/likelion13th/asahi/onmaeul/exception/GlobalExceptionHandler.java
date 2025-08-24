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

}
