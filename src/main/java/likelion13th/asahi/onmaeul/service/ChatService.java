package likelion13th.asahi.onmaeul.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import likelion13th.asahi.onmaeul.dto.request.ChatRequest;
import likelion13th.asahi.onmaeul.dto.response.ChatPreparePayload;
import likelion13th.asahi.onmaeul.dto.response.ChatResponsePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OpenAiService openAiService; // SDK 서비스 주입
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${openai.api.model}")
    private String modelName;

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

    /** 채팅 메시지 처리 및 응답 생성 (중요!!) */
    public ChatResponsePayload processChatMessage(ChatRequest chatRequest) {
        String sessionId = chatRequest.getSessionId();

        // 1. Redis에서 세션 정보 가져오기
        ChatResponsePayload.CollectedForm currentForm = getFormFromRedis(sessionId);

        // 2. 세션 상태 관리: 새로운 세션이거나 기존 세션이 만료된 경우 초기화
        if (currentForm == null) {
            sessionId = UUID.randomUUID().toString();
            currentForm = ChatResponsePayload.CollectedForm.builder().build();
        }

        // 3. LLM에 전달할 프롬프트 생성
        String prompt = buildPromptForLLM(chatRequest, currentForm);

        // 4. OpenAI API 호출 (SDK 사용)
        String llmResponseJson = openAiService.createChatCompletion(
                ChatCompletionRequest.builder()
                        .model(modelName)
                        .messages(List.of(new ChatMessage("user", prompt)))
                        .temperature(0.7)
                        .build()
        ).getChoices().get(0).getMessage().getContent();

        // 5. LLM 응답 파싱 및 DTO 생성
        ChatResponsePayload.CollectedForm updatedForm = parseLlmResponse(llmResponseJson, currentForm);

        // 6. Redis에 업데이트된 폼 저장
        saveFormToRedis(sessionId, updatedForm);

        // 7. 누락된 필드 및 완료 가능 여부 계산
        List<String> missingFields = getMissingFields(updatedForm);
        boolean canFinish = missingFields.isEmpty();

        // LLM이 지시한 action 확인 후, 필요한 경우 즉시 처리 (신규)
        String action = extractAction(llmResponseJson);
        String botReply;

        if ("CREATE".equalsIgnoreCase(action) && canFinish) {
            // 실제 폼 생성
            Long requestId = helpRequestService.create(updatedForm);   // ← 실제 서비스 호출
            // 세션 정리(선택)
            redisTemplate.delete("chat:session:" + sessionId);
            botReply = String.format("요청이 생성되었습니다! (ID: %d)\n마이페이지에서 확인하실 수 있어요.", requestId);

        } else if ("CONFIRM".equalsIgnoreCase(action) && canFinish) {
            // LLM이 요약/확인 단계로 판단 → LLM bot_reply 우선, 없으면 서버 요약
            try {
                JsonNode r = objectMapper.readTree(llmResponseJson);
                JsonNode br = r.get("bot_reply");
                botReply = (br != null && !br.isNull() && !br.asText().isBlank()) ? br.asText() : buildSummary(updatedForm);
            } catch (Exception e) {
                botReply = buildSummary(updatedForm);
            }

        } else if ("REVISE".equalsIgnoreCase(action)) {
            botReply = "어느 항목을 수정하시겠어요? (카테고리/장소/시간/제목/내용)";

        } else {
            // 8. 봇 응답 생성
            botReply = generateBotReply(updatedForm, canFinish, llmResponseJson);
        }

        // 최종 응답 페이로드 반환
        return ChatResponsePayload.builder()
                .session_id(sessionId)
                .bot_reply(botReply)
                .collected(updatedForm)
                .missing_fields(missingFields)
                .can_finish(canFinish)
                .session_ttl_seconds(1800)
                .build();
    }

    // Redis에서 폼 데이터 가져오기
    private ChatResponsePayload.CollectedForm getFormFromRedis(String sessionId) {
        if (sessionId == null) return null;
        Object formObject = redisTemplate.opsForValue().get("chat:session:" + sessionId);
        if (formObject instanceof ChatResponsePayload.CollectedForm) {
            return (ChatResponsePayload.CollectedForm) formObject;
        }
        return null;
    }

    // Redis에 폼 데이터 저장하기 (TTL 적용)
    private void saveFormToRedis(String sessionId, ChatResponsePayload.CollectedForm form) {
        redisTemplate.opsForValue().set("chat:session:" + sessionId, form, Duration.ofSeconds(1800)); // 30분 TTL
    }

    // LLM에 전달할 프롬프트 생성
    private String buildPromptForLLM(ChatRequest request, ChatResponsePayload.CollectedForm currentForm) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("당신은 '온마을' 서비스의 챗봇입니다. " +
                "채팅을 요청하는 사용자는 어르신이므로, 친절하고 존댓말을 사용하며, 쉽고 명확한 단어로 응답해야 합니다. " +
                "사용자의 채팅 메시지를 분석하여 도움 요청글에 필요한 정보를 추출하고, " +
                "누락된 정보가 있으면 다음 질문을 제시하세요. 질문은 한 번에 하나씩만 하세요.");
        promptBuilder.append("최종적으로 다음 JSON 형식으로 응답하세요.\n");
        promptBuilder.append("{\n" +
                "  \"data\": {\n" +                       // data 오브젝트로 감싸도록 고정
                "    \"category_id\": null,\n" +
                "    \"title\": null,\n" +
                "    \"description\": null,\n" +
                "    \"location\": null,\n" +
                "    \"location_detail\": null,\n" +
                "    \"phone_number\": null,\n" +
                "    \"request_time\": null,\n" +
                "    \"images\": []\n" +
                "  },\n" +
                "  \"missing_fields\": [],\n" +
                "  \"bot_reply\": \"질문 또는 요약\",\n" +
                "  \"action\": \"ASK | CONFIRM | CREATE | REVISE\"\n" +   // action 추가함!!
                "}\n");

        // action 가이드
        promptBuilder.append(
                "action 규칙: 필수값(카테고리/제목/내용/장소/시간) 중 하나라도 비면 ASK. " +
                        "모두 채워졌고 생성 의사(긍정)가 보이면 CREATE. " +
                        "모두 채워졌고 수정 의사(부정/수정)가 보이면 REVISE. " +
                        "모두 채워졌지만 확답 없으면 CONFIRM.\n" +
                        "CONFIRM일 때 bot_reply는 한 줄 요약 + '이대로 생성할까요?'로 해주세요.\n"
        );
        promptBuilder.append("기존 수집된 정보: ").append(objectMapper.valueToTree(currentForm).toString()).append("\n");
        promptBuilder.append("새로운 메시지: ").append(request.getMessage());

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            promptBuilder.append(" 첨부된 이미지 URL: ").append(request.getAttachments().get(0).getUrl());
        }

        return promptBuilder.toString();
    }

    // LLM 응답 파싱
    private ChatResponsePayload.CollectedForm parseLlmResponse(String llmResponseJson, ChatResponsePayload.CollectedForm currentForm) {
        try {
            JsonNode rootNode = objectMapper.readTree(llmResponseJson);
            JsonNode extractedData = rootNode.get("data");

            return ChatResponsePayload.CollectedForm.builder()
                    .category_id(extractedData.has("category_id") ? extractedData.get("category_id").asInt() : currentForm.getCategory_id())
                    .title(extractedData.has("title") ? extractedData.get("title").asText() : currentForm.getTitle())
                    .description(extractedData.has("description") ? extractedData.get("description").asText() : currentForm.getDescription())
                    .location(extractedData.has("location") ? extractedData.get("location").asText() : currentForm.getLocation())
                    .location_detail(extractedData.has("location_detail") ? extractedData.get("location_detail").asText() : currentForm.getLocation_detail())
                    .request_time(extractedData.has("request_time") ? extractedData.get("request_time").asText() : currentForm.getRequest_time())
                    .images(extractedData.has("images") ? objectMapper.convertValue(extractedData.get("images"), List.class) : currentForm.getImages())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }
    private String extractAction(String llmResponseJson) {
        try {
            JsonNode root = objectMapper.readTree(llmResponseJson);
            JsonNode n = root.get("action");
            return (n != null && !n.isNull()) ? n.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // 누락된 필드 확인
    private List<String> getMissingFields(ChatResponsePayload.CollectedForm form) {
        List<String> missing = new ArrayList<>();
        if (form.getCategory_id() == null) missing.add("category_id");
        if (form.getTitle() == null) missing.add("title");
        if (form.getDescription() == null) missing.add("description");
        if (form.getLocation() == null) missing.add("location");
        if (form.getRequest_time() == null) missing.add("request_time");
        return missing;
    }

    private String buildSummary(ChatResponsePayload.CollectedForm f) {
        List<String> parts = new ArrayList<>();
        if (f.getCategory_id() != null) parts.add("기기=" + f.getCategory_id());
        if (f.getLocation() != null) parts.add("장소=" + f.getLocation());
        if (f.getRequest_time() != null) parts.add("시간=" + f.getRequest_time());
        if (f.getTitle() != null) parts.add("제목=" + f.getTitle());
        if (f.getDescription() != null) parts.add("내용=" + (f.getDescription().length() > 40 ? f.getDescription().substring(0, 40) + "..." : f.getDescription()));
        String line = String.join(" · ", parts);
        return (line.isEmpty() ? "" : "정리: " + line + "\n") + "이대로 요청을 생성할까요?";
    }

    // 봇 응답 생성
    private String generateBotReply(ChatResponsePayload.CollectedForm form, boolean canFinish, String llmResponseJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(llmResponseJson);
            JsonNode botReplyNode = rootNode.get("bot_reply");
            if (botReplyNode != null) {
                return botReplyNode.asText();
            }
        } catch (Exception e) {
            return "죄송합니다. 오류가 발생했습니다. 다시 시도해 주세요.";
        }

        // 누락된 필드에 따라 답변 분기
        List<String> missingFields = getMissingFields(form);

        if (missingFields.contains("category_id")) {
            return "어떤 디지털 기기의 도움을 원하시는지 말씀해주세요. (스마트폰, 텔레비전, 키오스크)";
        } else if (missingFields.contains("location")) {
            return "만날 장소를 알려주세요. 집에서 도움이 필요하시면 정확한 집 주소를 적어주세요. 바깥에서 도움이 필요하시면 지하철역 출구나, 건물 이름도 좋아요!";
        } else if (missingFields.contains("request_time")) {
            return "언제 도움을 받고 싶으신가요? 날짜와 시간을 알려주세요!";
        } else if (missingFields.contains("title")) {
            return "요청하시는 내용의 제목을 간단하게 알려주세요.";
        } else if (missingFields.contains("description")) {
            return "어떤 도움이 필요한지 자세히 말씀해주세요.";
        }
        if (canFinish) {
            return String.format("정리해보면 %s에 %s에서 도움을 원하십니다. 이대로 요청 폼을 만들까요?", form.getRequest_time(), form.getLocation());
        }
        return "알겠습니다. 다른 정보를 알려주세요."; // 사실상 도달 X
    }
}

