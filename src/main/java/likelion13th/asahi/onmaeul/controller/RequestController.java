package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.response.request.RequestStatusPayload;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/request")
public class RequestController {
    private final RequestService requestService;

    /** 요청(Request) 탭 메인 화면 조회 */
    // 어르신과 청년 모두 접근 가능하도록 권한 수정
    @PreAuthorize("hasRole('SENIOR') or hasRole('JUNIOR')")
    @GetMapping("")
    public ResponseEntity<ApiResponse<RequestStatusPayload>> getRequestStatus(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        RequestStatusPayload data = requestService.getRequestStatus(me.getId());
        return ResponseEntity.ok(ApiResponse.ok("요청 상태 조회 성공", data));
    }
}
