package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.request.UpdateRequest;
import likelion13th.asahi.onmaeul.dto.response.requestTab.RequestDetailPayload;
import likelion13th.asahi.onmaeul.dto.response.requestTab.RequestStatusPayload;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/request")
public class RequestController {
    private final RequestService requestService;

    /** 요청(Request) 탭 메인 화면 조회 */
    // 어르신과 청년 모두 접근 가능하나 view가 다르다!
    @PreAuthorize("hasRole('SENIOR') or hasRole('JUNIOR')")
    @GetMapping("")
    public ResponseEntity<ApiResponse<RequestStatusPayload>> getRequestStatus(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        RequestStatusPayload data = requestService.getRequestStatus(me.getId());
        return ResponseEntity.ok(ApiResponse.ok("요청 상태 조회 성공", data));
    }

    /** 요청(Request) 탭 상세 화면 조회 */
    @PreAuthorize("hasRole('SENIOR') or hasRole('JUNIOR')")
    @GetMapping("/{request_id}")
    public ResponseEntity<ApiResponse<RequestDetailPayload>> getRequestDetails(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable("request_id") Long requestId
    ) {
        RequestDetailPayload data = requestService.getRequestDetails(me.getId(), requestId);
        return ResponseEntity.ok(ApiResponse.ok("요청 상세 조회 성공", data));
    }

    // 요청 수정 API (어르신)
    @PreAuthorize("hasRole('SENIOR')")
    @PatchMapping("/{request_id}")
    public ResponseEntity<ApiResponse<Void>> updateHelpRequest(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable("request_id") Long requestId,
            @RequestBody UpdateRequest request // 만들어둔 거 재사용함!
    ) {
        requestService.updateHelpRequest(me.getId(), requestId, request);
        return ResponseEntity.ok(ApiResponse.ok("요청이 수정되었습니다.", null));
    }

    // 요청 취소 API (어르신)
    @PreAuthorize("hasRole('SENIOR')")
    @DeleteMapping("/{request_id}")
    public ResponseEntity<ApiResponse<Void>> cancelHelpRequest(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable("request_id") Long requestId
    ) {
        requestService.cancelHelpRequest(me.getId(), requestId);
        return ResponseEntity.ok(ApiResponse.ok("요청이 취소되었습니다.", null));
    }

    // 도움 시작 API (어르신)
    @PreAuthorize("hasRole('SENIOR')")
    @PatchMapping("/{request_id}/start")
    public ResponseEntity<ApiResponse<Void>> startHelpRequest(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable("request_id") Long requestId
    ) {
        requestService.startHelpRequest(me.getId(), requestId);
        return ResponseEntity.ok(ApiResponse.ok("도움이 시작되었습니다.", null));
    }

    // 도움 완료 API (어르신)
    @PreAuthorize("hasRole('SENIOR')")
    @PatchMapping("/{request_id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeHelpRequest(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable("request_id") Long requestId
    ) {
        requestService.completeHelpRequest(me.getId(), requestId);
        return ResponseEntity.ok(ApiResponse.ok("도움이 완료되었습니다.", null));
    }
}
