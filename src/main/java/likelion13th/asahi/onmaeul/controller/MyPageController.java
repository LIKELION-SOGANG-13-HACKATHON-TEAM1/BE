package likelion13th.asahi.onmaeul.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import likelion13th.asahi.onmaeul.domain.ClassStatus;
import likelion13th.asahi.onmaeul.dto.request.EditMyPageRequest;
import likelion13th.asahi.onmaeul.dto.request.ImageRequest;
import likelion13th.asahi.onmaeul.dto.response.myPage.*;
import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage") // 공통 prefix
public class MyPageController {
    private final MyPageService myPageService;

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

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<EditMyPagePayload>> updateImage(@AuthenticationPrincipal CustomUserDetails me, @RequestBody ImageRequest imageRequest){
        EditMyPagePayload data=myPageService.updateImage(me.getId(),imageRequest.getImageFile());
        return ResponseEntity.ok(ApiResponse.ok("사진 수정 성공",data));
    }

//    @Operation(
//            summary = "프로필 사진 수정",
//            description = "사용자의 프로필 사진을 수정합니다. 이미지 파일을 multipart/form-data 형식으로 전송해야 합니다."
//    )
//    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse<EditMyPagePayload>> updateImage(
//            @AuthenticationPrincipal CustomUserDetails me,
//            // @RequestBody 대신 @RequestPart 사용
//            @RequestPart("imageFile") MultipartFile imageFile) {
//
//        // 서비스 메서드도 MultipartFile을 받도록 수정
//        EditMyPagePayload data = myPageService.updateImage(me.getId(), imageFile);
//        return ResponseEntity.ok(ApiResponse.ok("사진 수정 성공", data));
//    }

    @PreAuthorize("hasAnyRole('SENIOR','JUNIOR')")
    @PatchMapping
    public ResponseEntity<ApiResponse<EditMyPagePayload>> updateMyPage(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody EditMyPageRequest request
    ) {
        EditMyPagePayload data = myPageService.updateMyPage(me.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("내 정보 수정 성공", data));
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

    /** 받은 리뷰 상세 조회 (청년용) */
    @PreAuthorize("hasRole('JUNIOR')")
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailPayload>> getReceivedReviewDetail(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long reviewId
    ) {
        var data = myPageService.getReceivedReviewDetail(me.getId(), reviewId);
        return ResponseEntity.ok(ApiResponse.ok("받은 리뷰 상세 조회 성공", data));
    }

    /** 수업 신청 내역(수강 중인 수업 내역) 목록 조회 */
    @PreAuthorize("hasAnyRole('SENIOR', 'JUNIOR')")
    @GetMapping("/class")
    public ResponseEntity<ApiResponse<ClassListPayload>> getAppliedClasses(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam(required = false) ClassStatus filter_status
    ) {
        // 사용자의 ID를 서비스 메소드에 전달
        ClassListPayload data = myPageService.getAppliedClasses(me.getId(), filter_status);
        return ResponseEntity.ok(ApiResponse.ok("신청한 수업 목록 조회 성공", data));
    }

    /** 개설한 수업 내역 보기 */
    @PreAuthorize("hasAnyRole('SENIOR','JUNIOR')")
    @GetMapping("/classCourse")
    public ResponseEntity<ApiResponse<ClassListPayload>> getOpenedClasses(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam(name = "filter_status", required = false) ClassStatus filterStatus // 대문자: OPEN/CLOSED
    ) {
        ClassListPayload data = myPageService.getOpenedClasses(me.getId(), filterStatus);
        return ResponseEntity.ok(ApiResponse.ok("개설한 수업 목록 조회 성공", data));
    }

}
