package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.*;
import likelion13th.asahi.onmaeul.exception.NotFoundException;
import likelion13th.asahi.onmaeul.exception.UnauthorizedException;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import likelion13th.asahi.onmaeul.repository.MatchRepository;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {
    private final HelpRequestRepository helpRequestRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public void acceptHelpRequest(Long helpRequestId, Long responserId) {
        // 1. 도움 요청글 유효성 확인
        HelpRequest helpRequest = helpRequestRepository.findById(helpRequestId)
                .orElseThrow(() -> new NotFoundException("조회 가능한 요청이 없습니다."));

        // 2. 이미 매칭된 요청인지 확인 (중복 수락 방지)
        if (helpRequest.getStatus() != HelpRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 수락되었거나 종료된 요청입니다.");
        }

        // 3. 응답자(청년) 유효성 확인
        User responser = userRepository.findById(responserId)
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));

        // 4. Match 엔티티 생성
        Match newMatch = Match.builder()
                .helpRequest(helpRequest)
                .responser(responser)
                .status(MatchStatus.ACCEPTED)
                .build();

        matchRepository.save(newMatch);

        // 5. HelpRequest의 상태를 MATCHED로 변경
        helpRequest.setStatus(HelpRequestStatus.MATCHED);
        helpRequestRepository.save(helpRequest);
    }
}
