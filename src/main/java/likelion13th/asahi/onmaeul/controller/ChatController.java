package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.request.DraftRequest;
import likelion13th.asahi.onmaeul.dto.request.FinalChatRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.chat.DraftResponsePayload;
import likelion13th.asahi.onmaeul.dto.response.chat.FinalChatResponsePayload;
import likelion13th.asahi.onmaeul.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @PatchMapping("/draft")
    public ResponseEntity<ApiResponse<DraftResponsePayload>> patchForm(@AuthenticationPrincipal User user,
                                                                       @RequestBody DraftRequest draftRequest){
        ApiResponse<DraftResponsePayload> payload= chatService.update(draftRequest);

        return ResponseEntity.ok(payload);
    }

    @PostMapping("/finalize")
    public ResponseEntity<ApiResponse<FinalChatResponsePayload>> createArticle(@AuthenticationPrincipal User user, @RequestBody FinalChatRequest finalChatRequest){
        ApiResponse<FinalChatResponsePayload> payload=chatService.createArticle(finalChatRequest,user);
        return ResponseEntity.ok(payload);
    }
}
