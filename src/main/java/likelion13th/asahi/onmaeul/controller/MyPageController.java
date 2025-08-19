package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.response.EditMyPageResponse;
import likelion13th.asahi.onmaeul.dto.response.MyPageResponse;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    /* 로그인(세션)되어 있다고 가정 */
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal CustomUserDetails me // 주입받기
    ) {
        Long userId = me.getId(); // PK로 조회
        MyPageResponse body = myPageService.getMyPageById(userId);
        return ResponseEntity.ok(
                ApiResponse.ok("내정보 메인페이지 조회 성공", body)
        );
    }

    /* 수정 페이지 진입 (로그인 필요) */
    @GetMapping("/mypage/edit")
    public ResponseEntity<ApiResponse<EditMyPageResponse>> getMyPageEdit(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        Long userId = me.getId();
        EditMyPageResponse data = myPageService.getEditPageById(userId);

        return ResponseEntity.ok(
                ApiResponse.ok("내정보 수정페이지 조회 성공", data)
        );
    }

}
