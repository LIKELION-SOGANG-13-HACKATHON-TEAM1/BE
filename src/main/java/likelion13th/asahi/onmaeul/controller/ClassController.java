package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.domain.ClassStatus;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.clazz.ClazzPayload;
import likelion13th.asahi.onmaeul.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/class")
public class ClassController {
    private final ClassService classService;

    @GetMapping
    public ResponseEntity<ApiResponse<ClazzPayload>> getList(@RequestParam(defaultValue = "3") int limit, @RequestParam(value="cursor",required = false) String nextCursor, @RequestParam ClassStatus status) {
        ApiResponse<ClazzPayload> payload = classService.findList(nextCursor, status, limit);

        return ResponseEntity.ok(payload);
    }
}
