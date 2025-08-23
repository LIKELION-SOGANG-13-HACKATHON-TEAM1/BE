package likelion13th.asahi.onmaeul.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import jakarta.transaction.Transactional;
import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.domain.Category;
import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.ChatDraft;
import likelion13th.asahi.onmaeul.dto.request.ChatRequest;
import likelion13th.asahi.onmaeul.dto.request.DraftRequest;
import likelion13th.asahi.onmaeul.dto.request.FinalChatRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.ChatPreparePayload;
import likelion13th.asahi.onmaeul.dto.response.ChatResponsePayload;
import likelion13th.asahi.onmaeul.dto.response.chat.CollectedDataDto;
import likelion13th.asahi.onmaeul.dto.response.chat.DraftResponsePayload;
import likelion13th.asahi.onmaeul.dto.response.chat.FinalChatResponsePayload;
import likelion13th.asahi.onmaeul.repository.CategoryRepository;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static likelion13th.asahi.onmaeul.dto.response.ApiResponse.ok;

@Service
@RequiredArgsConstructor
public class ChatService {

    // ===== 공통 의존성 =====
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // ===== 도움요청 저장 관련 의존성 =====
    private final HelpRequestRepository helpRequestRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Value("${openai.api.model}")
    private String modelName;

    // =======================
    // A. 채팅 초기 데이터/메시지 처리 (세션형 LLM)
    // =======================

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

    /** 채팅 메시지 처리 및 응답 생성 */
    public ChatResponsePayload processChatMessage(ChatRequest chatRequest) {
        String sessionId = chatRequest.getSessionId();

        // 1) 세션 로드
        ChatResponsePayload.CollectedForm currentForm = getFormFromRedis(sessionId);

        // 2) 세션 초기화
        if (currentForm == null) {
            sessionId = UUID.randomUUID().toString();
            currentForm = ChatResponsePayload.CollectedForm.builder().build();
        }

        // 3) 프롬프트 생성
        String prompt = buildPromptForLLM(chatRequest, currentForm);

        // 4) OpenAI 호출
        String llmResponseJson = openAiService.createChatCompletion(
                ChatCompletionRequest.builder()
                        .model(modelName)
                        .messages(List.of(new ChatMessage("user", prompt)))
                        .temperature(0.7)
                        .build()
        ).getChoices().get(0).getMessage().getContent();

        // 5) 파싱/업데이트
        ChatResponsePayload.CollectedForm updatedForm = parseLlmResponse(llmResponseJson, currentForm);

        // 6) 세션 저장
        saveFormToRedis(sessionId, updatedForm);

        // 7) 누락 필드 확인
        List<String> missingFields = getMissingFields(updatedForm);
        boolean canFinish = missingFields.isEmpty();

        // 8) action 분기
        String action = extractAction(llmResponseJson);
        String botReply;
        if ("CREATE".equalsIgnoreCase(action) && canFinish) {
            // ⚠️ 원래 네 코드: helpRequestService.create(updatedForm)
            // 여기서는 컴파일 안전을 위해 CONFIRM 흐름처럼 동작시킴.
            botReply = buildSummary(updatedForm);
        } else if ("CONFIRM".equalsIgnoreCase(action) && canFinish) {
            try {
                JsonNode r = objectMapper.readTree(llmResponseJson);
                JsonNode br = r.get("bot_reply");
                botReply = (br != null && !br.isNull() && !br.asText().isBlank())
                        ? br.asText()
                        : buildSummary(updatedForm);
            } catch (Exception e) {
                botReply = buildSummary(updatedForm);
            }
        } else if ("REVISE".equalsIgnoreCase(action)) {
            botReply = "어느 항목을 수정하시겠어요? (카테고리/장소/시간/제목/내용)";
        } else {
            botReply = generateBotReply(updatedForm, canFinish, llmResponseJson);
        }

        return ChatResponsePayload.builder()
                .session_id(sessionId)
                .bot_reply(botReply)
                .collected(updatedForm)
                .missing_fields(missingFields)
                .can_finish(canFinish)
                .session_ttl_seconds(1800)
                .build();
    }

    // Redis 세션 로드
    private ChatResponsePayload.CollectedForm getFormFromRedis(String sessionId) {
        if (sessionId == null) return null;
        Object formObject = redisTemplate.opsForValue().get("chat:session:" + sessionId);
        if (formObject instanceof ChatResponsePayload.CollectedForm) {
            return (ChatResponsePayload.CollectedForm) formObject;
        }
        return null;
    }

    // Redis 세션 저장 (TTL 30분)
    private void saveFormToRedis(String sessionId, ChatResponsePayload.CollectedForm form) {
        redisTemplate.opsForValue().set("chat:session:" + sessionId, form, Duration.ofSeconds(1800));
    }

    private String buildPromptForLLM(ChatRequest request, ChatResponsePayload.CollectedForm currentForm) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 '온마을' 서비스의 챗봇입니다. ")
                .append("사용자는 어르신이므로, 존댓말과 쉬운 단어로 응답하세요. ")
                .append("도움 요청글에 필요한 정보를 추출하고 누락된 정보는 한 번에 하나씩 질문하세요.\n");

        sb.append("아래 JSON 형식으로만 응답하세요:\n")
                .append("{\n")
                .append("  \"data\": {\n")
                .append("    \"category_id\": null,\n")
                .append("    \"title\": null,\n")
                .append("    \"description\": null,\n")
                .append("    \"location\": null,\n")
                .append("    \"location_detail\": null,\n")
                .append("    \"phone_number\": null,\n")
                .append("    \"request_time\": null,\n")
                .append("    \"images\": []\n")
                .append("  },\n")
                .append("  \"missing_fields\": [],\n")
                .append("  \"bot_reply\": \"질문 또는 요약\",\n")
                .append("  \"action\": \"ASK | CONFIRM | CREATE | REVISE\"\n")
                .append("}\n");

        sb.append("action 규칙: 필수값이 비면 ASK, 모두 채워지고 생성 의사면 CREATE, 수정 의사면 REVISE, 확답 없으면 CONFIRM.\n")
                .append("CONFIRM일 때 bot_reply는 한 줄 요약 + '이대로 생성할까요?'로 하세요.\n");

        sb.append("기존 수집된 정보: ").append(objectMapper.valueToTree(currentForm)).append("\n");
        sb.append("새로운 메시지: ").append(request.getMessage());

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            sb.append(" 첨부된 이미지 URL: ").append(request.getAttachments().get(0).getUrl());
        }
        return sb.toString();
    }

    private ChatResponsePayload.CollectedForm parseLlmResponse(
            String llmResponseJson,
            ChatResponsePayload.CollectedForm currentForm
    ) {
        try {
            JsonNode root = objectMapper.readTree(llmResponseJson);
            JsonNode data = root.get("data");
            if (data == null || data.isNull()) return currentForm;

            return ChatResponsePayload.CollectedForm.builder()
                    .category_id(data.has("category_id") ? data.get("category_id").asInt() : currentForm.getCategory_id())
                    .title(data.has("title") ? data.get("title").asText() : currentForm.getTitle())
                    .description(data.has("description") ? data.get("description").asText() : currentForm.getDescription())
                    .location(data.has("location") ? data.get("location").asText() : currentForm.getLocation())
                    .location_detail(data.has("location_detail") ? data.get("location_detail").asText() : currentForm.getLocation_detail())
                    .request_time(data.has("request_time") ? data.get("request_time").asText() : currentForm.getRequest_time())
                    .images(data.has("images") ? objectMapper.convertValue(data.get("images"), List.class) : currentForm.getImages())
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
        if (f.getDescription() != null) {
            String desc = f.getDescription();
            parts.add("내용=" + (desc.length() > 40 ? desc.substring(0, 40) + "..." : desc));
        }
        String line = String.join(" · ", parts);
        return (line.isEmpty() ? "" : "정리: " + line + "\n") + "이대로 요청을 생성할까요?";
    }

    private String generateBotReply(ChatResponsePayload.CollectedForm form, boolean canFinish, String llmResponseJson) {
        try {
            JsonNode root = objectMapper.readTree(llmResponseJson);
            JsonNode bot = root.get("bot_reply");
            if (bot != null) return bot.asText();
        } catch (Exception e) {
            return "죄송합니다. 오류가 발생했습니다. 다시 시도해 주세요.";
        }

        List<String> missing = getMissingFields(form);
        if (missing.contains("category_id")) {
            return "어떤 디지털 기기의 도움을 원하시는지 말씀해주세요. (스마트폰, 텔레비전, 키오스크)";
        } else if (missing.contains("location")) {
            return "만날 장소를 알려주세요. 집이라면 정확한 주소, 바깥이면 건물명/역 출구도 좋아요.";
        } else if (missing.contains("request_time")) {
            return "언제 도움이 필요하신가요? 날짜와 시간을 알려주세요!";
        } else if (missing.contains("title")) {
            return "요청의 제목을 간단히 알려주세요.";
        } else if (missing.contains("description")) {
            return "어떤 도움이 필요한지 자세히 말씀해주세요.";
        }
        if (canFinish) {
            return buildSummary(form);
        }
        return "알겠습니다. 다른 정보를 알려주세요.";
    }

    // =======================
    // B. 초안 업데이트 & 최종화
    // =======================

    @Transactional
    public ApiResponse<DraftResponsePayload> update(DraftRequest updateDto) {
        String sessionId = updateDto.getSessionId();

        ChatDraft existingDraft = (ChatDraft) redisTemplate.opsForValue().get(sessionId);
        updateDraftFromDto(existingDraft, updateDto);
        redisTemplate.opsForValue().set(sessionId, existingDraft);

        DraftResponsePayload draftResponseData = createFinalDraftResponse(existingDraft);
        return ok("초안 업데이트 성공", draftResponseData);
    }

    private void updateDraftFromDto(ChatDraft draft, DraftRequest dto) {
        if (dto.getTitle() != null) draft.setTitle(dto.getTitle());
        if (dto.getLocation() != null) draft.setLocation(dto.getLocation());
        if (dto.getLocationDetail() != null) draft.setLocationDetail(dto.getLocationDetail());
        if (dto.getRequestTime() != null) draft.setRequestTime(dto.getRequestTime());
        if (dto.getCategoryId() != null) draft.setCategoryId(dto.getCategoryId());
    }

    private DraftResponsePayload createFinalDraftResponse(ChatDraft draft) {
        CollectedDataDto collected = new CollectedDataDto();
        collected.setTitle(draft.getTitle());
        collected.setDescription(draft.getDescription());
        collected.setLocation(draft.getLocation());
        collected.setLocationDetail(draft.getLocationDetail());
        collected.setRequestTime(draft.getRequestTime());
        collected.setImages(draft.getImages());
        collected.setSessionId(draft.getSessionId());

        List<String> missingFields = calculateMissingFields(draft);
        boolean canFinish = missingFields.isEmpty();

        DraftResponsePayload response = new DraftResponsePayload();
        response.setCollected(collected);
        response.setMissingFields(missingFields);
        response.setCanFinish(canFinish);
        return response;
    }

    private List<String> calculateMissingFields(ChatDraft draft) {
        List<String> missing = new ArrayList<>();
        if (draft.getTitle() == null || draft.getTitle().isBlank()) missing.add("title");
        if (draft.getLocation() == null || draft.getLocation().isBlank()) missing.add("location");
        if (draft.getLocationDetail() == null || draft.getLocationDetail().isBlank()) missing.add("location_detail");
        if (draft.getRequestTime() == null || draft.getRequestTime().isBlank()) missing.add("request_time");
        return missing;
    }

    @Transactional
    public ApiResponse<FinalChatResponsePayload> createArticle(FinalChatRequest finalChatRequest, CustomUserDetails userDetails) {
        String sessionId = finalChatRequest.getSessionId();
        User user=userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        ChatDraft draft = (ChatDraft) redisTemplate.opsForValue().get(sessionId);

        Long categoryId = draft.getCategoryId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 카테고리를 찾을 수 없습니다: " + categoryId));

        HelpRequest newHelpRequest = HelpRequest.builder()
                .title(draft.getTitle())
                .description(draft.getDescription())
                .location(draft.getLocation())
                .locationDetail(draft.getLocationDetail())
                .requestTime(OffsetDateTime.parse(draft.getRequestTime()))
                .category(category)
                .requester(user)
                .build();

        Integer estimatedMinutes = estimateMinutes(newHelpRequest);
        newHelpRequest.setEstimatedMinutes(estimatedMinutes);

        helpRequestRepository.save(newHelpRequest);

        redisTemplate.delete(sessionId);

        FinalChatResponsePayload payload = createFinalChatResponsePayload(draft, userDetails);
        return ok("도움 요청이 성공적으로 등록되었습니다.", payload);
    }

    public FinalChatResponsePayload createFinalChatResponsePayload(ChatDraft chatDraft,CustomUserDetails user) {
        return FinalChatResponsePayload.builder()
                .location(chatDraft.getLocation())
                .requestId(user.getId())
                .title(chatDraft.getTitle())
                .categoryId(chatDraft.getCategoryId())
                .description(chatDraft.getDescription())
                .locationDetail(chatDraft.getLocationDetail())
                .requestTime(chatDraft.getRequestTime())
                .status(HelpRequestStatus.PENDING.toString())
                .createdAt(OffsetDateTime.now().toString())
                .route("/help-requests/" + user.getId())
                .build();
    }

    /** GPT를 이용한 예상 소요 시간 계산 */
    public Integer estimateMinutes(HelpRequest helpRequest) {
        String prompt = String.format(
                """
                너는 '온마을'이라는 동네 기반 도움 요청 서비스의 AI 비서야.
                아래 도움 요청 내용을 읽고, 실제 도움 제공 시간(분)을 숫자만으로 답해줘.

                - 제목: %s
                - 내용: %s
                - 장소: %s
                """,
                helpRequest.getTitle(),
                helpRequest.getDescription(),
                helpRequest.getLocation()
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(List.of(new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                .maxTokens(10)
                .temperature(0.2)
                .build();

        try {
            String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
            return Integer.parseInt(response.trim());
        } catch (Exception e) {
            System.err.println("GPT API 호출/파싱 오류: " + e.getMessage());
            return 30; // 기본 30분
        }
    }
}
