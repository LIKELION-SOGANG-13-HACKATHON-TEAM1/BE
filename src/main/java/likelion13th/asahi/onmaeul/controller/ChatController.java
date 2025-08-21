package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.dto.request.ChatRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.ChatPreparePayload;
import likelion13th.asahi.onmaeul.dto.response.ChatResponsePayload;
import likelion13th.asahi.onmaeul.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    /** 채팅 초기 데이터 조회 (어르신) */
    @PreAuthorize("hasRole('SENIOR')")
    @GetMapping("/prepare")
    public ResponseEntity<ApiResponse<ChatPreparePayload>> getInitialChatData(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ChatPreparePayload data = chatService.getInitialChatData();
        return ResponseEntity.ok(ApiResponse.ok("채팅 초기 데이터 조회 성공", data));
    }

    /** 채팅 메시지 처리 (어르신) */
    @PreAuthorize("hasRole('SENIOR')")
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatResponsePayload>> processChatMessages(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody ChatRequest request
    ) {
        ChatResponsePayload data = chatService.processChatMessage(request);
        return ResponseEntity.ok(ApiResponse.ok("메시지 처리 성공", data));
    }
}

