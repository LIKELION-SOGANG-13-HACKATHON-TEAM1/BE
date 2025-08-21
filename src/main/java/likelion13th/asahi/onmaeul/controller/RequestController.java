package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.service.RequestService;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasRole('SENIOR')")
    @GetMapping("")
    public ResponseEntity<ApiResponse<RequestStatusPayload>> getRequestStatus(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        RequestStatusPayload data = requestService.getRequestStatus(me.getId());
        return ResponseEntity.ok(ApiResponse.ok("요청 상태 조회 성공", data));
    }
}
