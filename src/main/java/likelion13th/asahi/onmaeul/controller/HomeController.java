package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.home.GuestHomePayload;
import likelion13th.asahi.onmaeul.dto.response.home.HomeAction;
import likelion13th.asahi.onmaeul.dto.response.home.HomePayload;
import likelion13th.asahi.onmaeul.config.props.GuestHomeProps;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/home")
public class HomeController {
    private final HomeService homeService;
    private final GuestHomeProps guestHomeProps;


    @GetMapping
    public ResponseEntity<ApiResponse<? extends HomePayload>> getHome(@AuthenticationPrincipal User user){
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

        ApiResponse<?extends HomePayload> payload=homeService.getHome(user);
        return ResponseEntity.ok(payload);

        }

    }



