package likelion13th.asahi.onmaeul.controller;

import jakarta.transaction.Transactional;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.helpRequest.DeletePayload;
import likelion13th.asahi.onmaeul.dto.response.helpRequest.HelpRequestArticlePayload;
import likelion13th.asahi.onmaeul.dto.response.helpRequest.HelpRequestPayload;
import likelion13th.asahi.onmaeul.dto.response.helpRequest.UpdatePayload;
import likelion13th.asahi.onmaeul.dto.response.home.GuestHomePayload;
import likelion13th.asahi.onmaeul.dto.response.home.HomeAction;
import likelion13th.asahi.onmaeul.dto.response.home.HomePayload;
import likelion13th.asahi.onmaeul.config.props.GuestHomeProps;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.service.HelpRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/helpRequests")
public class HelpRequestController {
    private final HelpRequestService helpRequestService;
    private final GuestHomeProps guestHomeProps;

    @GetMapping
    public ResponseEntity<ApiResponse<? extends HomePayload>> getMain(@RequestParam(defaultValue = "5") int limitFeed,
                                                                      @RequestParam(required = false) String nextCursor, @AuthenticationPrincipal User user ){

        ApiResponse<HelpRequestPayload> payload=helpRequestService.findMain(nextCursor,HelpRequestStatus.PENDING,user.getRole(),limitFeed);

        return ResponseEntity.ok(payload);

    }

    //요청글 상세보기
    @GetMapping("/{request_id}")
    public ResponseEntity<ApiResponse<HelpRequestArticlePayload>> getArticle(@PathVariable("request_id")Long id,@AuthenticationPrincipal User user){

        ApiResponse<HelpRequestArticlePayload> payload=helpRequestService.findArticle(id,user);
        return ResponseEntity.ok(payload);
    }

    //요청글 삭제하기
    @DeleteMapping("/{request_id}")
    public ResponseEntity<ApiResponse<DeletePayload>> deleteArticle(@PathVariable("request_id")Long id,@AuthenticationPrincipal User user){
        ApiResponse< DeletePayload > payload=helpRequestService.deleteArticle(id,user);
        return ResponseEntity.ok(payload);
    }

    //요청글 수정하기
    @PatchMapping("/{request_id}")
    public ResponseEntity<ApiResponse<UpdatePayload>> patchArticle(@PathVariable("request_id")Long id,@AuthenticationPrincipal User user){
        ApiResponse<UpdatePayload> payload=helpRequestService.patch(id,user);
        return ResponseEntity.ok(payload);
    }
}
