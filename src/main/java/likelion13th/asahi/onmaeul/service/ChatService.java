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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static likelion13th.asahi.onmaeul.dto.response.ApiResponse.ok;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    // ===== 공통 의존성 =====
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

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
    // 기존 메서드 전체 교체
    public ChatResponsePayload processChatMessage(ChatRequest chatRequest) {

        // 1) 세션 아이디 확정 2) 세션 상태 로드
        String sessionId = (chatRequest.getSessionId() == null || chatRequest.getSessionId().isBlank())
                ? UUID.randomUUID().toString()
                : chatRequest.getSessionId();

        ChatResponsePayload.CollectedForm currentForm = getFormFromRedis(sessionId);
        if (currentForm == null) {
            currentForm = ChatResponsePayload.CollectedForm.builder().build();
        }

        // 3) 프롬프트 생성
        String prompt = buildPromptForLLM(chatRequest, currentForm);
        log.info("LLM prompt size={}, modelName={}", prompt.length(), modelName);
        log.debug("LLM prompt content: {}", prompt);

        // 4) OpenAI 호출
        String llmRaw = null;
        String jsonText = null;
        try {
            var chatCompletionResponse = openAiService.createChatCompletion(
                    ChatCompletionRequest.builder()
                            .model(modelName)
                            .messages(List.of(
                                    // ✅ 출력 규칙은 system에 넣어서 강하게 고정
                                    new ChatMessage(ChatMessageRole.SYSTEM.value(),
                                            "You are the Onmaeul assistant. " +
                                                    "Output ONLY valid JSON. No code fences, no explanations, no extra text. " +
                                                    "All string values (e.g., bot_reply, title, description, location) MUST be in Korean and use polite speech. " +
                                                    "All timestamps MUST be ISO 8601 with timezone offset +09:00 (Asia/Seoul). " +
                                                    "Example: \"2025-08-25T15:00:00+09:00\". " +
                                                    "If the user provides a relative time (e.g., \"내일 오후 3시\"), convert it to ISO 8601 with +09:00; " +
                                                    "if the date is missing, ask one follow-up question to clarify. " +
                                                    "Schema: { \"data\": {\"category_id\": null|number, \"title\": null|string, \"description\": null|string, " +
                                                    "\"location\": null|string, \"location_detail\": null|string, " +
                                                    "\"request_time\": null|string, \"images\": []}, \"missing_fields\": [], " +
                                                    "\"bot_reply\": string, \"action\": \"ASK|CONFIRM|CREATE|REVISE\" }"
                                    ),
                                    // 사용자 컨텍스트는 user에
                                    new ChatMessage(ChatMessageRole.USER.value(), prompt)
                            ))
                            .temperature(0.2) // 일탈 최소화
                            .maxTokens(600)
                            .build()
            );

            if (chatCompletionResponse == null
                    || chatCompletionResponse.getChoices() == null
                    || chatCompletionResponse.getChoices().isEmpty()) {
                throw new IllegalStateException("OpenAI로부터 유효한 응답을 받지 못했습니다. 빈 응답입니다.");
            }

            llmRaw = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
            log.info("Received LLM response: {}", llmRaw);

            // ✅ 코드펜스/군더더기 제거 → JSON만 추출
            jsonText = extractJsonBlock(llmRaw);
            log.debug("Sanitized LLM JSON: {}", jsonText);

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return ChatResponsePayload.builder()
                    .session_id(sessionId)
                    .bot_reply("죄송합니다. 현재 챗봇 서비스에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.")
                    .collected(currentForm)
                    .missing_fields(getMissingFields(currentForm))
                    .can_finish(false)
                    .session_ttl_seconds(1800)
                    .build();
        }

        // 5) 파싱/업데이트 (정제된 JSON 사용)
        ChatResponsePayload.CollectedForm updatedForm;
        try {
            updatedForm = parseLlmResponse(jsonText, currentForm); // ← 여기!
            log.info("Parsed and updated form: {}", updatedForm);
        } catch (Exception e) {
            log.error("LLM 응답 JSON 파싱 중 오류 발생: {} (원본 JSON: {})", e.getMessage(), llmRaw, e);

            // UX 개선: 사과문 대신 누락 필드 기반 질문으로 이어가기
            List<String> missing = getMissingFields(currentForm);
            String fallbackReply = generateBotReply(currentForm, missing.isEmpty(), "{\"bot_reply\":\"\"}");

            return ChatResponsePayload.builder()
                    .session_id(sessionId)
                    .bot_reply(fallbackReply)
                    .collected(currentForm)
                    .missing_fields(missing)
                    .can_finish(false)
                    .session_ttl_seconds(1800)
                    .build();
        }

        // 6) 세션 저장
        try {
            saveFormToRedis(sessionId, updatedForm);
        } catch (Exception e) {
            log.error("Redis에 세션 저장 중 오류 발생: {}", sessionId, e);
        }

        // 7) 누락 필드 확인
        List<String> missingFields = getMissingFields(updatedForm);
        boolean canFinish = missingFields.isEmpty();

        // 8) action 분기 (정제된 JSON 사용)
        String action = extractAction(jsonText);
        String botReply;
        if ("CREATE".equalsIgnoreCase(action) && canFinish) {
            // 여기서 초안 저장 !!!!
            saveDraft(sessionId, updatedForm);

            // 요약 멘트(원하면 유지)
            botReply = buildSummary(updatedForm);

            // ★ 응답에 action 같이 내려서 프론트가 바로 finalize 호출하게
            return ChatResponsePayload.builder()
                    .session_id(sessionId)
                    .bot_reply(botReply)
                    .collected(updatedForm)
                    .missing_fields(missingFields)
                    .can_finish(true)
                    .session_ttl_seconds(1800)
                    .action("CREATE")
                    .build();
        }
        else if ("CONFIRM".equalsIgnoreCase(action) && canFinish) {
            try {
                JsonNode r = objectMapper.readTree(jsonText);
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
            botReply = generateBotReply(updatedForm, canFinish, jsonText);
        }

        return ChatResponsePayload.builder()
                .session_id(sessionId)
                .bot_reply(botReply)
                .collected(updatedForm)
                .missing_fields(missingFields)
                .can_finish(canFinish)
                .session_ttl_seconds(1800)
                .action(action)
                .build();
    }

    // ✅ 저장: 무조건 JSON 문자열로 + 새 프리픽스(v2)
    private void saveFormToRedis(String sessionId, ChatResponsePayload.CollectedForm form) {
        if (sessionId == null || sessionId.isBlank()) return;
        try {
            String key = "chat:session:v2:" + sessionId; // ★ 레거시 분리
            String json = objectMapper.writeValueAsString(form);
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofSeconds(1800));
        } catch (Exception e) {
            log.error("Redis save error: sid={}, err={}", sessionId, e.toString(), e);
        }
    }

    // ✅ 읽기: String(JSON) → DTO
    private ChatResponsePayload.CollectedForm getFormFromRedis(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return null;
        try {
            String key = "chat:session:v2:" + sessionId; // ★ 레거시 분리
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return objectMapper.readValue(json, ChatResponsePayload.CollectedForm.class);
        } catch (Exception e) {
            log.error("Redis load error: sid={}, err={}", sessionId, e.toString(), e);
            return null;
        }
    }

    private String buildPromptForLLM(ChatRequest request, ChatResponsePayload.CollectedForm currentForm) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 '온마을' 서비스의 챗봇입니다. ")
                .append("사용자는 어르신이므로, 존댓말과 쉬운 단어로 응답하세요. ")
                .append("도움 요청글에 필요한 정보를 추출하고 누락된 정보는 한 번에 하나씩 질문하세요.\n")
                .append("반드시 유효한 JSON만 응답하세요. 코드펜스( ``` )나 추가 설명을 절대 붙이지 마세요.\n")
                .append("작성 지침(요약/정제):\n")
                .append("- 사용자의 원문을 그대로 복사하지 말고, 맞춤법을 보정해 핵심만 간결하게 재작성하세요.\n")
                .append("- title 은 20자 내외의 간결한 존댓말 명령형으로 작성하세요. 예: \"은행 앱 설치 도와주세요\".\n")
                .append("- description 은 2~3문장으로 목적/상황/필요한 도움을 정리하고, 불필요한 감탄사·중복·속어는 제거하세요.\n")
                .append("- 시간·장소·기기는 구체적으로 적되, 어려운 전문 용어·영어는 쉬운 말로 풀어쓰세요.\n")
                .append("- 상대시간(예: 내일 오후 3시)은 Asia/Seoul 기준 ISO 8601(+09:00)으로 변환하세요. 모호하면 한 번만 되물어보세요.\n")
                .append("- 전화번호는 매칭 후 자동 공유되므로 절대 묻지 마세요. missing_fields 에 phone_number 를 넣지 마세요.\n")
                .append("- 추가 질문이 필요할 때는 한 번에 하나의 짧은 질문만 하세요.\n");
        ;
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

        sb.append("기존 수집된 정보: ").append(objectMapper.valueToTree(currentForm).toString()).append("\n");
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

    // ChatService.java (클래스 내부, 다른 private 메서드들과 같은 위치)
    private String extractJsonBlock(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.startsWith("```")) {
            t = t.replaceFirst("(?s)^```[a-zA-Z]*\\s*", "");
            t = t.replaceFirst("(?s)\\s*```\\s*$", "");
            t = t.trim();
        }
        int s = t.indexOf('{'), e = t.lastIndexOf('}');
        return (s >= 0 && e > s) ? t.substring(s, e + 1) : t;
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

    private static final Duration DRAFT_TTL = Duration.ofSeconds(1800);
    private void saveDraft(String sessionId, ChatResponsePayload.CollectedForm f) {
        try {
            ChatDraft draft = new ChatDraft();
            draft.setSessionId(sessionId);
            draft.setTitle(f.getTitle());
            draft.setDescription(f.getDescription());
            draft.setLocation(f.getLocation());
            draft.setLocationDetail(f.getLocation_detail());
            draft.setRequestTime(f.getRequest_time());
            draft.setImages(f.getImages());
            // category_id 타입 맞춰서
            if (f.getCategory_id() != null) {
                draft.setCategoryId(f.getCategory_id().longValue());
            }

            String key = "chat:draft:v1:" + sessionId; // ★ 드래프트 전용 키
            String json = objectMapper.writeValueAsString(draft);
            stringRedisTemplate.opsForValue().set(key, json, DRAFT_TTL);
        } catch (Exception e) {
            log.error("saveDraft error: sid={}, err={}", sessionId, e.toString(), e);
        }
    }

    // =======================
    // B. 초안 업데이트 & 최종화
    // =======================

    @Transactional
    public ApiResponse<DraftResponsePayload> update(DraftRequest updateDto) {
        String sessionId = updateDto.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId가 없습니다.");
        }

        String key = "chat:draft:v1:" + sessionId;
        try {
            String json = stringRedisTemplate.opsForValue().get(key);

            ChatDraft draft = (json != null && !json.isBlank())
                    ? objectMapper.readValue(json, ChatDraft.class)   // ← checked exception
                    : new ChatDraft();

            draft.setSessionId(sessionId);
            updateDraftFromDto(draft, updateDto);

            String updated = objectMapper.writeValueAsString(draft);  // ← checked exception
            stringRedisTemplate.opsForValue().set(key, updated, DRAFT_TTL);

            DraftResponsePayload resp = createFinalDraftResponse(draft);
            return ok("초안 업데이트 성공", resp);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("초안 JSON 처리 실패: sid={}, err={}", sessionId, e.toString(), e);
            throw new IllegalStateException("초안 데이터 형식이 올바르지 않습니다.", e);
        } catch (Exception e) {
            log.error("초안 업데이트 실패: sid={}, err={}", sessionId, e.toString(), e);
            throw new IllegalStateException("초안 업데이트 중 오류가 발생했습니다.", e);
        }
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

        String key = "chat:draft:v1:" + sessionId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("세션 초안을 찾을 수 없습니다. 다시 시도해 주세요.");
        }
        ChatDraft draft;
        try {
            draft = objectMapper.readValue(json, ChatDraft.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("초안 JSON 파싱 실패: sid={}, json={}", sessionId, json, e);
            // 트랜잭션 롤백을 원하면 체크예외 말고 런타임 예외로 던지는 게 좋아요
            throw new IllegalStateException("초안 데이터 형식이 올바르지 않습니다.", e);
        }

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
                .images(draft.getImages())
                .build();

        Integer estimatedMinutes = estimateMinutes(newHelpRequest);
        newHelpRequest.setEstimatedMinutes(estimatedMinutes);

        HelpRequest saved = helpRequestRepository.save(newHelpRequest);

        // 드래프트 삭제
        stringRedisTemplate.delete(key);

        FinalChatResponsePayload payload = createFinalChatResponsePayload(draft, saved);
        return ok("도움 요청이 성공적으로 등록되었습니다.", payload);
    }

    private FinalChatResponsePayload createFinalChatResponsePayload(ChatDraft draft, HelpRequest saved) {
        return FinalChatResponsePayload.builder()
                .requestId(saved.getId())
                .title(saved.getTitle())
                .categoryId(
                        saved.getCategory() != null ? saved.getCategory().getId()
                                : draft.getCategoryId()
                )
                .description(saved.getDescription())
                .location(saved.getLocation())
                .locationDetail(saved.getLocationDetail())
                .requestTime(
                        saved.getRequestTime() != null
                                ? saved.getRequestTime().toString()  // ISO-8601(+offset)
                                : draft.getRequestTime()
                )
                .status(HelpRequestStatus.PENDING.toString())
                .createdAt(OffsetDateTime.now().toString())
                .route("/help-requests/" + saved.getId())
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
