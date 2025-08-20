package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.Match;
import likelion13th.asahi.onmaeul.domain.Review;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.response.myPage.*;
import likelion13th.asahi.onmaeul.repository.MatchRepository;
import likelion13th.asahi.onmaeul.repository.ReviewRepository;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ReviewRepository reviewRepository;

    public MyPagePayload getMyPageById(Long userId) {
        // DB에서 User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음: id=" + userId));

        // DTO 변환해서 반환
        return MyPagePayload.builder()
                .user_id(user.getId())
                .user_phonenumber(user.getPhoneNumber())
                .user_introduce(user.getIntroduce())
                .user_name(user.getUsername())   // 닉네임 없이 이름만 사용
                .build();
    }

    @Transactional(readOnly = true)
    public EditMyPagePayload getEditPageById(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음: id=" + userId));

        return EditMyPagePayload.builder()
                .user_id(u.getId())
                .user_name(u.getUsername())
                .birth_date(u.getBirthDate() == null ? null : u.getBirthDate().toString()) // 선택 필드
                .user_phonenumber(u.getPhoneNumber())
                .user_introduce(u.getIntroduce())
                .profile_url(u.getProfileUrl())
                .build();
    }

    /** 도움 신청 내역(어르신이 도움 받은 내역) 리스트 */
    @Transactional(readOnly = true)
    public HelpListPayload getReceivedHelpList(Long seniorUserId) { // size, cursor 제거

        List<Match> matches = matchRepository.findAllForSenior(seniorUserId); // 페이징 없이 전체 조회

        var items = matches.stream()
                .map(m -> HelpItemPayload.builder()
                        .match_id(m.getId())
                        .help_title(m.getHelpRequest().getTitle())
                        .matched_at(toIso(m.getMatchedAt()))
                        .location(m.getHelpRequest().getLocation())
                        .build())
                .collect(Collectors.toList());

        PagingInfo paging = PagingInfo.builder()
                .all(true)
                .count(items.size())
                .has_next(false)
                .next_cursor(null)
                .build();

        return HelpListPayload.builder()
                .help_list(items)
                .paging(paging)
                .build();
    }

    private String toIso(OffsetDateTime odt) {
        return odt == null ? null : odt.toString();
    }

    @Transactional(readOnly = true)
    public HelpReceivedDetailPayload getReceivedHelpDetail(Long seniorId, Long matchId) {
        // 권한/소유 확인: 로그인한 어르신의 매칭만 조회됨
        Match m = matchRepository.findDetailForSenior(matchId, seniorId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다."));

        HelpRequest hr = m.getHelpRequest();

        // 소요시간 "X시간 Y분" 가공 (없으면 null)
        String durationText = null;
        Integer minutes = hr.getEstimatedMinutes(); // 프로젝트 엔티티에 맞게 사용
        if (minutes != null) {
            int h = minutes / 60, mm = minutes % 60;
            durationText = (h > 0 ? h + "시간 " : "") + mm + "분";
        }

        // 첨부 이미지 URL 목록 (없으면 빈 리스트)
        List<String> images = hr.getImages();

        // 화면 전용 페이로드 구성
        return HelpReceivedDetailPayload.builder()
                .match_id(m.getId())
                .title(hr.getTitle())
                .requester_name(hr.getRequester().getUsername())
                .location(hr.getLocation())
                .request_time(formatKorean(hr.getRequestTime())) // "YYYY.MM.DD HH:mm"
                .duration_text(durationText)
                .description(hr.getDescription())
                .images(images)
                .build();
    }

    // 화면 표기에 맞춘 한국형 날짜 포맷(필요 시 ISO-8601로 내려도 됨)
    private String formatKorean(OffsetDateTime t) {
        if (t == null) return null;
        return t.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }

    /** 도움 제공 내역(청년이 도움 준 내역) 리스트 - 페이징 없이 전체 반환 */
    @Transactional(readOnly = true)
    public HelpListPayload getOfferedHelpList(Long youthUserId) {

        // 청년 ID(responserId)를 통해 매칭 리스트를 조회
        List<Match> matches = matchRepository.findAllByResponserId(youthUserId);

        // DTO 매핑
        var items = matches.stream()
                .map(m -> HelpItemPayload.builder()
                        .match_id(m.getId())
                        .help_title(m.getHelpRequest().getTitle())
                        .matched_at(toIso(m.getMatchedAt()))
                        .location(m.getHelpRequest().getLocation())
                        .build())
                .collect(Collectors.toList());

        // 페이징 정보 (명세서에 맞게 all=true)
        PagingInfo paging = PagingInfo.builder()
                .all(true)
                .count(items.size())
                .has_next(false)
                .next_cursor(null)
                .build();

        return HelpListPayload.builder()
                .help_list(items)
                .paging(paging)
                .build();
    }

    @Transactional(readOnly = true)
    public HelpOfferedDetailPayload getOfferedHelpDetail(Long juniorId, Long matchId) {
        // 권한/소유 확인: 로그인한 청년의 매칭만 조회됨
        Match m = matchRepository.findDetailForJunior(matchId, juniorId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다."));

        HelpRequest hr = m.getHelpRequest();

        // 소요시간 "X시간 Y분" 가공
        String durationText = null;
        Integer minutes = hr.getEstimatedMinutes();
        if (minutes != null) {
            int h = minutes / 60, mm = minutes % 60;
            durationText = (h > 0 ? h + "시간 " : "") + mm + "분";
        }

        // 리뷰 정보
        Double rating = (m.getReview() != null) ? m.getReview().getRating() : null;
        String review = (m.getReview() != null) ? m.getReview().getContent() : null;

        // DTO 구성
        return HelpOfferedDetailPayload.builder()
                .match_id(m.getId())
                .title(hr.getTitle())
                .requester_name(hr.getRequester().getUsername())
                .location(hr.getLocation())
                .request_time(formatKorean(hr.getRequestTime()))
                .duration_text(durationText)
                .description(hr.getDescription())
                .images(hr.getImages())
                .rating_stars(rating)
                .review_text(review)
                .build();
    }

    /** 받은 리뷰 목록 조회 (청년용) */
    @Transactional(readOnly = true)
    public ReviewListPayload getReceivedReviewList(Long juniorId) {
        // 청년 ID(juniorId)를 통해 받은 리뷰 리스트를 조회
        List<Review> reviews = reviewRepository.findAllByJuniorId(juniorId);

        // DTO 매핑
        var items = reviews.stream()
                .map(r -> ReviewItemPayload.builder()
                        .id(r.getId())
                        .content(r.getContent())
                        .rating(r.getRating())
                        .build())
                .collect(Collectors.toList());

        // 페이징 정보 (명세서에 맞게 all=true)
        PagingInfo paging = PagingInfo.builder()
                .all(true)
                .count(items.size())
                .has_next(false)
                .next_cursor(null)
                .build();

        return ReviewListPayload.builder()
                .review_list(items)
                .paging(paging)
                .build();
    }

}
