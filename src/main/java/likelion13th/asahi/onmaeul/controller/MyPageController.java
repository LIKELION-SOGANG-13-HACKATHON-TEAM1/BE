package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.response.myPage.EditMyPagePayload;
import likelion13th.asahi.onmaeul.dto.response.myPage.HelpListPayload;
import likelion13th.asahi.onmaeul.dto.response.myPage.HelpReceivedDetailPayload;
import likelion13th.asahi.onmaeul.dto.response.myPage.MyPagePayload;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage") // 공통 prefix
public class MyPageController {
    private final MyPageService myPageService;

    /* 로그인(세션)되어 있다고 가정 */
    @GetMapping
    public ResponseEntity<ApiResponse<MyPagePayload>> getMyPage(
            @AuthenticationPrincipal CustomUserDetails me // 주입받기
    ) {
        Long userId = me.getId(); // PK로 조회
        MyPagePayload body = myPageService.getMyPageById(userId);
        return ResponseEntity.ok(
                ApiResponse.ok("내정보 메인페이지 조회 성공", body)
        );
    }

    /* 수정 페이지 진입 (로그인 필요) */
    @GetMapping("/edit")
    public ResponseEntity<ApiResponse<EditMyPagePayload>> getMyPageEdit(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long userId = me.getId();
        EditMyPagePayload data = myPageService.getEditPageById(userId);

        return ResponseEntity.ok(
                ApiResponse.ok("내정보 수정페이지 조회 성공", data)
        );
    }
    /* 도움 신청 내역(어르신이 도움 받은 내역) 리스트 조회 */
    @PreAuthorize("hasRole('SENIOR')")
    @GetMapping("/request")
    public ResponseEntity<ApiResponse<HelpListPayload>> getMyReceivedHelp(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long cursor
    ) {
        HelpListPayload data = myPageService.getReceivedHelpList(me.getId(), size, cursor);
        return ResponseEntity.ok(ApiResponse.ok("도움 받은 내역 조회 성공", data));
    }

    @PreAuthorize("hasRole('SENIOR')")
    @GetMapping("/request/{matchId}")
    public ResponseEntity<ApiResponse<HelpReceivedDetailPayload>> getReceivedHelpDetail(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long matchId
    ) {
        var data = myPageService.getReceivedHelpDetailMinimal(me.getId(), matchId);
        return ResponseEntity.ok(ApiResponse.ok("도움 받은 내역 상세페이지 조회 성공", data));
    }
}
