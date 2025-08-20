package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.response.myPage.*;
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

    /** 도움 신청 내역(어르신이 도움 받은 내역) 리스트 조회 */
    @PreAuthorize("hasRole('SENIOR')")
    @GetMapping("/request")
    public ResponseEntity<ApiResponse<HelpListPayload>> getMyReceivedHelp(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long cursor
    ) {
        HelpListPayload data = myPageService.getReceivedHelpList(me.getId());
        return ResponseEntity.ok(ApiResponse.ok("도움 받은 내역 조회 성공", data));
    }

    /** 도움 신청 내역(어르신이 도움 받은 내역) 상세 글 조회 */
    @PreAuthorize("hasRole('SENIOR')")
    @GetMapping("/request/{matchId}")
    public ResponseEntity<ApiResponse<HelpReceivedDetailPayload>> getReceivedHelpDetail(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long matchId
    ) {
        var data = myPageService.getReceivedHelpDetail(me.getId(), matchId);
        return ResponseEntity.ok(ApiResponse.ok("도움 받은 내역 상세페이지 조회 성공", data));
    }

    /** 도움 수락 내역(청년이 도움 준 내역) 리스트 조회 */
    @PreAuthorize("hasRole('JUNIOR')")
    @GetMapping("/offer")
    public ResponseEntity<ApiResponse<HelpListPayload>> getMyOfferedHelp(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        // 청년의 ID를 서비스 메소드에 전달
        HelpListPayload data = myPageService.getOfferedHelpList(me.getId());
        return ResponseEntity.ok(ApiResponse.ok("도움 준 내역 조회 성공", data));
    }

    /** 도움 수락 내역(청년이 도움 준 내역) 상세 글 조회 */
    @PreAuthorize("hasRole('JUNIOR')")
    @GetMapping("/offer/{matchId}")
    public ResponseEntity<ApiResponse<HelpOfferedDetailPayload>> getOfferedHelpDetail(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long matchId
    ) {
        var data = myPageService.getOfferedHelpDetail(me.getId(), matchId);
        return ResponseEntity.ok(ApiResponse.ok("도움 준 내역 상세페이지 조회 성공", data));
    }

    /** 받은 리뷰 목록 조회 (청년용) */
    @PreAuthorize("hasRole('JUNIOR')")
    @GetMapping("/review")
    public ResponseEntity<ApiResponse<ReviewListPayload>> getReceivedReviewList(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        // 청년의 ID를 서비스 메소드에 전달
        ReviewListPayload data = myPageService.getReceivedReviewList(me.getId());
        return ResponseEntity.ok(ApiResponse.ok("받은 리뷰 내역 조회 성공", data));
    }
}
