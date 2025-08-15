package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.DTO.response.SeniorHomeResponse;
import likelion13th.asahi.onmaeul.service.HomeService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/home")
public class HomeController {
    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<SeniorHomeResponse> showHome(@RequestParam(defaultValue = "5") int limitFeed,
                                                       @RequestParam(required = false) String nextCursor,
                                                       @AuthenticationPrincipal CustomUserDetails user)


}
