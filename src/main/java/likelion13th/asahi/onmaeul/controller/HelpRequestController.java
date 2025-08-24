package likelion13th.asahi.onmaeul.controller;

import jakarta.transaction.Transactional;
import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.request.UpdateRequest;
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
import org.apache.catalina.util.CustomObjectInputStream;
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
    public ResponseEntity<ApiResponse<HelpRequestPayload>> getMain(@RequestParam(defaultValue = "5") int limitFeed,
                                                                      @RequestParam(required = false) String nextCursor, @AuthenticationPrincipal CustomUserDetails user ){

        ApiResponse<HelpRequestPayload> payload=helpRequestService.findMain(nextCursor,HelpRequestStatus.PENDING,user.getRole(),limitFeed);

        return ResponseEntity.ok(payload);

    }

    //요청 리스트 검색 기능
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<HelpRequestPayload>> searchHelpRequests(@RequestParam("keyword") String keyword,
                                                                              @RequestParam(defaultValue="5")int size,
                                                                              @RequestParam(defaultValue = "0")int page,
                                                                              @AuthenticationPrincipal CustomUserDetails user){
        ApiResponse<HelpRequestPayload> payload=helpRequestService.search(keyword,user.getRole(),page,size);
        return ResponseEntity.ok(payload);
    }

    //요청글 상세보기
    @GetMapping("/{request_id}")
    public ResponseEntity<ApiResponse<HelpRequestArticlePayload>> getArticle(@PathVariable("request_id")Long id,@AuthenticationPrincipal CustomUserDetails user){

        ApiResponse<HelpRequestArticlePayload> payload=helpRequestService.findArticle(id,user);
        return ResponseEntity.ok(payload);
    }

    //요청글 삭제하기
    @DeleteMapping("/{request_id}")
    public ResponseEntity<ApiResponse<DeletePayload>> deleteArticle(@PathVariable("request_id")Long id,@AuthenticationPrincipal CustomUserDetails user){
        ApiResponse< DeletePayload > payload=helpRequestService.deleteArticle(id,user);
        return ResponseEntity.ok(payload);
    }

    //요청글 수정하기
    @PatchMapping("/{request_id}")
    public ResponseEntity<ApiResponse<UpdatePayload>> patchArticle(@PathVariable("request_id")Long id, @AuthenticationPrincipal CustomUserDetails user, @RequestBody UpdateRequest updateRequest){
        ApiResponse<UpdatePayload> payload=helpRequestService.patch(id,user,updateRequest);
        return ResponseEntity.ok(payload);
    }
}
