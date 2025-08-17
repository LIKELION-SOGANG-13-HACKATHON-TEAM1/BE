package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.DTO.response.ApiResponse;
import likelion13th.asahi.onmaeul.DTO.response.helpRequest.HelpRequestItem;
import likelion13th.asahi.onmaeul.DTO.response.helpRequest.HelpRequestPayload;
import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static likelion13th.asahi.onmaeul.DTO.response.ApiResponse.ok;


@RequiredArgsConstructor
@Service
public class HelpRequestService {
    private final HelpRequestRepository helpRequestRepository;


    public ApiResponse<HelpRequestPayload> findMain(){
        List<HelpRequestItem> helpRequestItems= helpRequestRepository.findTop5ByStatusOrderByCreatedAtDescIdDesc("PENDING")
                .stream()
                .map(HelpRequestItem::fromEntity)
                .toList();
        HelpRequestPayload helpRequestPayload=HelpRequestPayload.builder()
                .helpRequestItems(helpRequestItems).build();

        return ok("어르신용 도움 요청 리스트 조회 성공",helpRequestPayload);
    }

    public HelpRequest save(HelpRequest helpRequest) {
        return helpRequestRepository.save(helpRequest);
    }
}
