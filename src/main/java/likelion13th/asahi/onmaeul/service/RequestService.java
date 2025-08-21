package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.response.request.RequestStatusPayload;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final UserRepository userRepository;
    private final HelpRequestRepository helpRequestRepository;

    public RequestStatusPayload getRequestStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found")); // 예외 처리

        if ("SENIOR".equals(user.getRole())) {
            List<HelpRequest> requests = helpRequestRepository.findBySeniorId(userId);

            if (requests.isEmpty()) {
                return RequestStatusPayload.builder()
                        .role("senior")
                        .state("none")
                        .requests(List.of())
                        .build();
            } else {
                List<RequestDto> dtoList = requests.stream()
                        .map(request -> RequestDto.builder()
                                .requestId(request.getId())
                                .title(request.getTitle())
                                .location(request.getLocation())
                                .requestTime(request.getRequestTime().toString())
                                .status(request.getStatus().toString().toLowerCase())
                                .actions(getActions(request.getStatus().toString()))
                                .juniorInfo(request.getJunior() != null ? JuniorInfo.from(request.getJunior()) : null)
                                .build())
                        .collect(Collectors.toList());

                return RequestStatusPayload.builder()
                        .role("senior")
                        .state("active_requests")
                        .requests(dtoList)
                        .build();
            }

        } else if ("JUNIOR".equals(user.getRole())) {
            List<HelpRequest> requests = helpRequestRepository.findByJuniorIdAndStatusIn(userId, List.of("accepted", "in_progress"));

            if (requests.isEmpty()) {
                return RequestStatusPayload.builder()
                        .role("junior")
                        .state("none")
                        .acceptedRequests(List.of())
                        .build();
            } else {
                List<AcceptedRequestDto> dtoList = requests.stream()
                        .map(request -> AcceptedRequestDto.builder()
                                .requestId(request.getId())
                                .title(request.getTitle())
                                .location(request.getLocation())
                                .requestTime(request.getRequestTime().toString())
                                .status(request.getStatus().toString().toLowerCase())
                                .seniorInfo(request.getSenior() != null ? SeniorInfo.from(request.getSenior()) : null)
                                .build())
                        .collect(Collectors.toList());

                return RequestStatusPayload.builder()
                        .role("junior")
                        .state("active_requests")
                        .acceptedRequests(dtoList)
                        .build();
            }

        } else {
            throw new IllegalArgumentException("Invalid user role"); // 유효하지 않은 역할
        }
    }

    private ActionsDto getActions(String status) {
        boolean canEdit = "pending".equalsIgnoreCase(status);
        boolean canDelete = "pending".equalsIgnoreCase(status);
        return ActionsDto.builder()
                .canEdit(canEdit)
                .canDelete(canDelete)
                .build();
    }
}
