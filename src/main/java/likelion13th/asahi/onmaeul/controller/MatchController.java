package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.request.AcceptMatchRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {
    private final MatchService matchService;

    /** 도움 요청 수락하기 (청년) */
    @PreAuthorize("hasRole('JUNIOR')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> acceptHelpRequest(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody AcceptMatchRequest request
    ) {
        if (me == null) throw new UnauthorizedException("로그인이 필요합니다."); // 401로 매핑
        matchService.acceptHelpRequest(request.getHelpRequestId(), me.getId());
        return ResponseEntity.ok(ApiResponse.ok("도움 요청 수락 성공", null));
    }
}
