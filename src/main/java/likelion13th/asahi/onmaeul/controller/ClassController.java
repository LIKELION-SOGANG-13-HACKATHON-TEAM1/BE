package likelion13th.asahi.onmaeul.controller;

import jakarta.validation.Payload;
import likelion13th.asahi.onmaeul.domain.ClassStatus;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.clazz.ClazzArticlePayload;
import likelion13th.asahi.onmaeul.dto.response.clazz.ClazzParticipatePayload;
import likelion13th.asahi.onmaeul.dto.response.clazz.ClazzPayload;
import likelion13th.asahi.onmaeul.dto.response.helpRequest.HelpRequestPayload;
import likelion13th.asahi.onmaeul.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/class")
public class ClassController {
    private final ClassService classService;

    @GetMapping
    public ResponseEntity<ApiResponse<ClazzPayload>> getList(@RequestParam(defaultValue = "3") int limit, @RequestParam(value = "cursor", required = false) String nextCursor, @RequestParam ClassStatus status) {
        ApiResponse<ClazzPayload> payload = classService.findList(nextCursor, status, limit);

        return ResponseEntity.ok(payload);
    }

    //요청 리스트 검색 기능
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ClazzPayload>> searchHelpRequests(@RequestParam("keyword") String keyword,
                                                                              @RequestParam(defaultValue="3")int size,
                                                                              @RequestParam(defaultValue = "0")int page,
                                                                              @RequestParam ClassStatus status){
        ApiResponse<ClazzPayload> payload=classService.search(keyword,page,size,status);
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/{class_id}")
    public ResponseEntity<ApiResponse<ClazzArticlePayload>> getArticle(@PathVariable("class_id") Long id){
        ApiResponse<ClazzArticlePayload> payload=classService.findArticle(id);
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/{class_id}/participants")
    public ResponseEntity<ApiResponse<ClazzParticipatePayload>> applyClass(@PathVariable("class_id") Long id, @AuthenticationPrincipal User user){
        ApiResponse<ClazzParticipatePayload> payload=classService.applyClass(id,user.getId());
        return ResponseEntity.ok(payload);
    }


}
