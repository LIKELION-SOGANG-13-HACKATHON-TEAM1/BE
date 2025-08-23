package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import likelion13th.asahi.onmaeul.domain.Match;
import likelion13th.asahi.onmaeul.domain.Review;
import likelion13th.asahi.onmaeul.dto.request.ReviewRequest;
import likelion13th.asahi.onmaeul.dto.response.ReviewResponsePayload;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import likelion13th.asahi.onmaeul.repository.MatchRepository;
import likelion13th.asahi.onmaeul.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MatchRepository matchRepository;
    private final HelpRequestRepository helpRequestRepository;

    @Transactional
    public ReviewResponsePayload createReview(Long seniorId, ReviewRequest payload) {
        Match match = matchRepository.findById(payload.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("매칭 정보를 찾을 수 없습니다."));

        // 1. 리뷰 작성 권한 및 상태 유효성 검사
        //    - 로그인한 사용자가 요청 글 작성자인지 확인
        if (!match.getHelpRequest().getRequester().getId().equals(seniorId)) {
            throw new IllegalArgumentException("리뷰 작성 권한이 없습니다.");
        }
        //    - 매칭 상태가 COMPLETED_UNREVIEWED인지 확인
        if (match.getHelpRequest().getStatus() != HelpRequestStatus.COMPLETED_UNREVIEWED) {
            throw new IllegalArgumentException("리뷰는 도움 완료 상태에서만 작성 가능합니다.");
        }

        // 2. 리뷰 엔티티 생성 및 저장
        Review newReview = Review.builder()
                .match(match)
                .writer(match.getHelpRequest().getRequester())
                .rating(payload.getRating())
                .content(payload.getContent())
                .build();
        reviewRepository.save(newReview);

        // 3. HelpRequest 상태를 최종 완료 상태로 업데이트
        match.getHelpRequest().setStatus(HelpRequestStatus.COMPLETED_REVIEWED);
        helpRequestRepository.save(match.getHelpRequest());

        // 4. 응답 DTO 구성 및 반환
        return ReviewResponsePayload.builder()
                .reviewId(newReview.getId())
                .matchId(match.getId())
                .targetId(payload.getTargetId())
                .rating(newReview.getRating())
                .createdAt(newReview.getCreatedAt())
                .build();
    }
}