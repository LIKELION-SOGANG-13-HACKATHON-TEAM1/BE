package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.Match;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.response.myPage.*;
import likelion13th.asahi.onmaeul.repository.MatchRepository;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /**
     * 도움 신청 내역(어르신이 도움 받은 내역) 리스트
     * size, cursor 모두 null → 전체 반환
     * size>0 → 커서 페이징 (cursor=null이면 최신부터)
     */
    @Transactional(readOnly = true)
    public HelpListPayload getReceivedHelpList(Long seniorUserId, Integer size, Long cursor) {

        final boolean all = (size == null && cursor == null); // 전체 조회 여부를 판단

        List<Match> matches;
        if (all) {
            matches = matchRepository.findAllForSenior(seniorUserId);
        } else {
            int limit = (size == null || size <= 0) ? 20 : size;
            // limit+1 로 조회하여 has_next 판단
            matches = matchRepository.findSliceForSenior(
                    seniorUserId,
                    cursor,                           // null이면 최신부터
                    (Pageable) PageRequest.of(0, limit + 1)
            );
        }

        // DTO 매핑
        var items = matches.stream()
                .map(m -> HelpItemPayload.builder()
                        .match_id(m.getId())
                        .help_title(m.getHelpRequest().getTitle())       // HelpRequest.title 가정
                        .matched_at(toIso(m.getMatchedAt()))             // Match.matchedAt
                        .location(m.getHelpRequest().getLocation())      // HelpRequest.location 가정
                        .build())
                .collect(Collectors.toList());

        PagingInfo paging;
        if (all) {
            paging = PagingInfo.builder()
                    .all(true)
                    .count(items.size())
                    .has_next(false)
                    .next_cursor(null)
                    .build();
        } else {
            int limit = (size == null || size <= 0) ? 20 : size;
            boolean hasNext = items.size() > limit;
            if (hasNext) {
                items = items.subList(0, limit);
            }
            String nextCursor = hasNext
                    ? String.valueOf(items.get(items.size() - 1).getMatch_id())
                    : null;

            paging = PagingInfo.builder()
                    .all(false)
                    .count(items.size())
                    .has_next(hasNext)
                    .next_cursor(nextCursor)
                    .build();
        }

        return HelpListPayload.builder()
                .help_list(items)
                .paging(paging)
                .build();
    }

    private String toIso(OffsetDateTime odt) {
        return odt == null ? null : odt.toString();
    }

    @Transactional(readOnly = true)
    public HelpReceivedDetailPayload getReceivedHelpDetailMinimal(Long seniorId, Long matchId) {
        // 1) 권한/소유 확인: 로그인한 어르신의 매칭만 조회됨
        Match m = matchRepository.findDetailForSenior(matchId, seniorId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다."));

        HelpRequest hr = m.getHelpRequest();

        // 2) 소요시간 "X시간 Y분" 가공 (없으면 null)
        String durationText = null;
        Integer minutes = hr.getEstimatedMinutes(); // 프로젝트 엔티티에 맞게 사용
        if (minutes != null) {
            int h = minutes / 60, mm = minutes % 60;
            durationText = (h > 0 ? h + "시간 " : "") + mm + "분";
        }

        // 3) 첨부 이미지 URL 목록 (없으면 빈 리스트)
        List<String> images = hr.getImages();

        // 4) 리뷰(선택)
        Double rating = (m.getReview() != null) ? m.getReview().getRating() : null;
        String  review = (m.getReview() != null) ? m.getReview().getContent() : null;

        // 5) 화면 전용 페이로드 구성
        return HelpReceivedDetailPayload.builder()
                .match_id(m.getId())
                .title(hr.getTitle())
                .requester_name(hr.getRequester().getUsername())
                .location(hr.getLocation())
                .request_time(formatKorean(hr.getRequestTime())) // "YYYY.MM.DD HH:mm"
                .duration_text(durationText)
                .description(hr.getDescription())
                .images(images)
                .rating_stars(rating)
                .review_text(review)
                .build();
    }

    // 화면 표기에 맞춘 한국형 날짜 포맷(필요 시 ISO-8601로 내려도 됨)
    private String formatKorean(OffsetDateTime t) {
        if (t == null) return null;
        return t.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }

}
