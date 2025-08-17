package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.DTO.response.ApiResponse;
import likelion13th.asahi.onmaeul.DTO.response.helpRequest.HelpRequestItem;
import likelion13th.asahi.onmaeul.DTO.response.helpRequest.HelpRequestPayload;
import likelion13th.asahi.onmaeul.DTO.response.home.GuestHomePayload;
import likelion13th.asahi.onmaeul.DTO.response.home.HomeAction;
import likelion13th.asahi.onmaeul.service.HelpRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/helpRequests")
public class HelpRequestController {
    private final HelpRequestService helpRequestService;

    @GetMapping
    public ResponseEntity<ApiResponse<HelpRequestPayload>> getMain(@RequestParam(defaultValue = "5") int limitFeed,
                                                      @RequestParam(required = false) String nextCursor, Authentication auth){

        return ResponseEntity.ok(helpRequestService.findMain());
    }
}
