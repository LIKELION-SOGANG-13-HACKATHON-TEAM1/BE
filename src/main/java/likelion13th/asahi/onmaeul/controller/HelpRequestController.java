package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.DTO.response.ApiResponse;
import likelion13th.asahi.onmaeul.DTO.response.helpRequest.HelpRequestPayload;
import likelion13th.asahi.onmaeul.DTO.response.home.GuestHomePayload;
import likelion13th.asahi.onmaeul.DTO.response.home.HomeAction;
import likelion13th.asahi.onmaeul.DTO.response.home.HomePayload;
import likelion13th.asahi.onmaeul.config.props.GuestHomeProps;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.service.HelpRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/helpRequests")
public class HelpRequestController {
    private final HelpRequestService helpRequestService;
    private final GuestHomeProps guestHomeProps;

    @GetMapping
    public ResponseEntity<ApiResponse<? extends HomePayload>> getMain(@RequestParam(defaultValue = "5") int limitFeed,
                                                                      @RequestParam(required = false) String nextCursor, @AuthenticationPrincipal User user ){

        //guest용
        if (user==null) {
            GuestHomePayload payload = GuestHomePayload.builder()
                    .role("guest")
                    .guestAction(new HomeAction(
                            guestHomeProps.title(), guestHomeProps.subtitle(),
                            guestHomeProps.route(), guestHomeProps.action()))
                    .build();
            return ResponseEntity.ok(ApiResponse.ok(guestHomeProps.message(), payload));
        }

        //사용자 role 확인
        ApiResponse<HelpRequestPayload> payload=helpRequestService.findMain(nextCursor,HelpRequestStatus.PENDING,user.getRole(),limitFeed);

        return ResponseEntity.ok(payload);

    }
}
