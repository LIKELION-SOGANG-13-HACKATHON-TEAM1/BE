package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.request.ReviewRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.ReviewResponsePayload;
import likelion13th.asahi.onmaeul.service.ReviewService;
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
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasRole('SENIOR')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<ReviewResponsePayload>> createReview(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody ReviewRequest request
    ) {
        ReviewResponsePayload data = reviewService.createReview(me.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("리뷰가 등록되었습니다.", data));
    }
}