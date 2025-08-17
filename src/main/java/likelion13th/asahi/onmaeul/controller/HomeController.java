package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.DTO.response.ApiResponse;
import likelion13th.asahi.onmaeul.DTO.response.home.GuestHomePayload;
import likelion13th.asahi.onmaeul.DTO.response.home.HomeAction;
import likelion13th.asahi.onmaeul.DTO.response.home.HomePayload;
import likelion13th.asahi.onmaeul.config.props.GuestHomeProps;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/home")
public class HomeController {
    private final HomeService homeService;
    private final GuestHomeProps guestHomeProps;


    @GetMapping
    public ResponseEntity<ApiResponse<? extends HomePayload>> getHome(Authentication auth){
        //guest 여부 체크
        boolean guest = (auth == null) || (auth instanceof AnonymousAuthenticationToken);

        //guest용
        if (guest) {
            GuestHomePayload payload = GuestHomePayload.builder()
                    .role("guest")
                    .guestAction(new HomeAction(
                            guestHomeProps.title(), guestHomeProps.subtitle(),
                            guestHomeProps.route(), guestHomeProps.action()))
                    .build();
            return ResponseEntity.ok(ApiResponse.ok(guestHomeProps.message(), payload));
        }


        //사용자 role 확인
        User user=resolveUser(auth);

        ApiResponse<?extends HomePayload> payload=homeService.getHome(user);
        return ResponseEntity.ok(payload);



        }

    //User 값 받아오기
    //로그인 코드 작성 후 수정 필요
    private User resolveUser(Authentication auth){
        Object principal = auth.getPrincipal();
        if(principal instanceof User u)
            return u;
        else return null;
    }


    }



