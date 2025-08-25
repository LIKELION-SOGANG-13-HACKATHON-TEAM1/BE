package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.*;
import likelion13th.asahi.onmaeul.dto.request.UpdateRequest;
import likelion13th.asahi.onmaeul.dto.response.requestTab.*;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import likelion13th.asahi.onmaeul.repository.MatchRepository;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final UserRepository userRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final MatchRepository matchRepository;

    public RequestStatusPayload getRequestStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found")); // 예외 처리

        if (UserRole.SENIOR.equals(user.getRole())) {
            List<HelpRequest> allRequests = helpRequestRepository.findByRequesterId(userId);

            if (allRequests.isEmpty()) {
                return RequestStatusPayload.builder()
                        .role("senior")
                        .state("none")
                        .pendingRequests(List.of())
                        .acceptedRequests(List.of())
                        .build();
            } else {
                List<HelpRequest> acceptedAndInProgress = allRequests.stream()
                        .filter(req -> req.getStatus() == HelpRequestStatus.MATCHED || req.getStatus() == HelpRequestStatus.IN_PROGRESS)
                        .collect(Collectors.toList());

                // acceptedAndInProgress 리스트를 기반으로 Match 엔티티를 찾음
                Map<Long, Match> matchesByRequestId = matchRepository.findByHelpRequestIn(acceptedAndInProgress).stream()
                        .collect(Collectors.toMap(match -> match.getHelpRequest().getId(), match -> match));

                List<PendingRequestPayload> pendingRequests = allRequests.stream()
                        .filter(req -> req.getStatus() == HelpRequestStatus.PENDING)
                        .map(req -> PendingRequestPayload.builder()
                                .requestId(req.getId())
                                .title(req.getTitle())
                                .location(req.getLocation())
                                .requestTime(req.getRequestTime().toString())
                                .status(req.getStatus().toString().toLowerCase())
                                .actions(getActions(req.getStatus()))
                                .build())
                        .collect(Collectors.toList());

                List<AcceptedRequestPayload> acceptedRequests = acceptedAndInProgress.stream()
                        .map(req -> {
                            Match match = matchesByRequestId.get(req.getId());
                            JuniorInfo juniorInfo = (match != null && match.getResponser() != null) ? JuniorInfo.from(match.getResponser()) : null;

                            return AcceptedRequestPayload.builder()
                                    .requestId(req.getId())
                                    .title(req.getTitle())
                                    .location(req.getLocation())
                                    .requestTime(req.getRequestTime().toString())
                                    .status(req.getStatus().toString().toLowerCase())
                                    .actions(getActions(req.getStatus()))
                                    .juniorInfo(juniorInfo)
                                    .build();
                        })
                        .collect(Collectors.toList());

                return RequestStatusPayload.builder()
                        .role("senior")
                        .state("active_requests")
                        .pendingRequests(pendingRequests)
                        .acceptedRequests(acceptedRequests)
                        .build();
            }
        } else if (UserRole.JUNIOR.equals(user.getRole())) {
            List<Match> matches = matchRepository.findByResponserAndHelpRequestStatus(
                    userId,
                    List.of(HelpRequestStatus.MATCHED, HelpRequestStatus.IN_PROGRESS)
            );

            if (matches.isEmpty()) {
                return RequestStatusPayload.builder()
                        .role("junior")
                        .state("none")
                        .acceptedRequests(List.of())
                        .build();
            } else {
                List<AcceptedRequestPayload> acceptedRequests = matches.stream()
                        .map(match -> {
                            HelpRequest request = match.getHelpRequest();
                            User senior = request.getRequester();
                            return AcceptedRequestPayload.builder()
                                    .requestId(request.getId())
                                    .title(request.getTitle())
                                    .location(request.getLocation())
                                    .requestTime(request.getRequestTime().toString())
                                    .status(request.getStatus().toString().toLowerCase())
                                    .seniorInfo(SeniorInfo.from(senior))
                                    .build();
                        })
                        .collect(Collectors.toList());

                return RequestStatusPayload.builder()
                        .role("junior")
                        .state("active_requests")
                        .acceptedRequests(acceptedRequests)
                        .build();
            }

        } else {
            throw new IllegalArgumentException("Invalid user role"); // 유효하지 않은 역할
        }
    }
    private ActionsDto getActions(HelpRequestStatus status) {
        boolean canEdit = HelpRequestStatus.PENDING.equals(status);
        boolean canDelete = HelpRequestStatus.PENDING.equals(status);
        return ActionsDto.builder()
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }

    public RequestDetailPayload getRequestDetails(Long userId, Long requestId) {
        // 사용자 정보 + 요청 글 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        // DTO 빌드 (공통 정보)
        RequestDetailPayload.RequestDetailPayloadBuilder builder = RequestDetailPayload.builder()
                .requestId(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .images(request.getImages())
                .location(request.getLocation())
                .requestTime(request.getRequestTime().toString())
                .status(request.getStatus().toString().toLowerCase())
                .category(request.getCategory().getName());

        // 상태 및 역할에 따라 추가 정보 및 액션 버튼 설정
        if (UserRole.SENIOR.equals(user.getRole())) {
            // 어르신용 DTO 구성
            builder.seniorInfo(SeniorInfo.from(user));
            Optional<Match> matchOpt = matchRepository.findByHelpRequest(request);

            if (matchOpt.isPresent()) {
                Match match = matchOpt.get();
                User junior = match.getResponser();
                builder.juniorInfo(JuniorInfo.from(junior));
                builder.actions(getSeniorActionsForMatch(request.getStatus()));
            } else {
                builder.actions(getSeniorActionsForPending(request.getStatus()));
            }
        } else if (UserRole.JUNIOR.equals(user.getRole())) {
            // 청년용 DTO 구성: 어르신 정보만 담음!! (본인 정보와 버튼은 제외)
            User senior = request.getRequester();
            builder.seniorInfo(SeniorInfo.from(senior));
        }

        return builder.build();
    }

    private ActionsDto getSeniorActionsForPending(HelpRequestStatus status) {
        // 요청이 PENDING 상태일 때만 수정/취소 버튼이 활성화됩니다.
        boolean canEdit = (status == HelpRequestStatus.PENDING);
        boolean canDelete = (status == HelpRequestStatus.PENDING);

        return ActionsDto.builder()
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }
    private ActionsDto getSeniorActionsForMatch(HelpRequestStatus status) {
        // 요청이 매칭되었을 때 '도움 시작' 버튼이 활성화됩니다.
        boolean canStart = (status == HelpRequestStatus.MATCHED);

        // 요청이 진행 중일 때 '도움 완료' 버튼이 활성화됩니다.
        boolean canComplete = (status == HelpRequestStatus.IN_PROGRESS);

        // 어르신은 리뷰를 남길 수 있는 상태도 고려해야 합니다.
        boolean canReview = (status == HelpRequestStatus.COMPLETED_UNREVIEWED);

        return ActionsDto.builder()
                .canStart(canStart)
                .canComplete(canComplete)
                .canReview(canReview)
                .build();
    }

    @Transactional
    public void updateHelpRequest(Long userId, Long requestId, UpdateRequest request) {
        HelpRequest helpRequest = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        validateSeniorAction(userId, helpRequest, HelpRequestStatus.PENDING, "수정");

        // 요청글의 제목, 내용, 이미지를 업데이트
        helpRequest.update(request);
        helpRequestRepository.save(helpRequest);
    }

    @Transactional
    public void cancelHelpRequest(Long userId, Long requestId) {
        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        validateSeniorAction(userId, request, HelpRequestStatus.PENDING, "취소");

        request.setStatus(HelpRequestStatus.CANCELED);
        helpRequestRepository.save(request);
    }

    @Transactional
    public RequestStartPayload startHelpRequest(Long userId, Long requestId) {
        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        // 어르신 권한 + 현재 상태 확인 (요청이 MATCHED 여야 시작 가능)
        validateSeniorAction(userId, request, HelpRequestStatus.MATCHED, "도움 시작");

        // 1) 매칭 찾기 (권한까지 체크하는 버전 권장)
        Match match = matchRepository.findActiveForSenior(requestId, userId, MatchStatus.ACCEPTED)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청의 활성 매칭이 없습니다."));

        // 2) 상태 전환 + 시간 기록 (둘 다)
        OffsetDateTime now = OffsetDateTime.now();

        request.setStatus(HelpRequestStatus.IN_PROGRESS);
        helpRequestRepository.save(request);

        match.setStatus(MatchStatus.IN_PROGRESS);  // 엔티티에 세터/도메인 메서드가 있어야 함
        matchRepository.save(match);

        // 3) 응답 DTO로 match_id 포함해 반환
        return RequestStartPayload.builder()
                .match_id(match.getId())
                .request_id(request.getId())
                .status(HelpRequestStatus.IN_PROGRESS)
                .build();
    }

    @Transactional
    public void completeHelpRequest(Long userId, Long requestId) {
        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        validateSeniorAction(userId, request, HelpRequestStatus.IN_PROGRESS, "도움 완료");

        request.setStatus(HelpRequestStatus.COMPLETED_UNREVIEWED);
        helpRequestRepository.save(request);
    }

    private void validateSeniorAction(Long userId, HelpRequest request, HelpRequestStatus requiredStatus, String actionName) {
        if (!request.getRequester().getId().equals(userId)) {
            throw new IllegalArgumentException(actionName + "할 권한이 없습니다.");
        }
        if (!request.getStatus().equals(requiredStatus)) {
            throw new IllegalArgumentException(actionName + "는 " + requiredStatus + " 상태에서만 가능합니다.");
        }
    }

}