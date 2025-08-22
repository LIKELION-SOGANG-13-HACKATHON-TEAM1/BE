package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.*;
import likelion13th.asahi.onmaeul.dto.response.requestTab.*;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import likelion13th.asahi.onmaeul.repository.MatchRepository;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
        } else if ("JUNIOR".equals(user.getRole())) {
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
}
