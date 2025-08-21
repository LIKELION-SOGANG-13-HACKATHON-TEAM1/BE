package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.dto.response.ChatPreparePayload;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class ChatService {
    public ChatPreparePayload getInitialChatData() {
        return ChatPreparePayload.builder()
                .session_id(UUID.randomUUID().toString())
                .greeting("안녕하세요! 무엇을 도와드릴까요?")
                .tips(Arrays.asList(
                        "도움 요청 기기, 일시, 장소를 알려주세요!",
                        "＋ 버튼을 누르면 사진을 첨부할 수 있어요!"
                ))
                .suggested_chats(Arrays.asList(
                        "핸드폰 화면 녹화하는 방법을 알려주세요!",
                        "어플을 다운로드 받고 싶어요!"
                ))
                .build();
    }
}
