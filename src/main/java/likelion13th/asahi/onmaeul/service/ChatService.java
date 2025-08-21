package likelion13th.asahi.onmaeul.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import jakarta.transaction.Transactional;
import likelion13th.asahi.onmaeul.domain.Category;
import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.ChatDraft;
import likelion13th.asahi.onmaeul.dto.request.DraftRequest;
import likelion13th.asahi.onmaeul.dto.request.FinalChatRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.chat.CollectedDataDto;
import likelion13th.asahi.onmaeul.dto.response.chat.DraftResponsePayload;
import likelion13th.asahi.onmaeul.dto.response.chat.FinalChatResponsePayload;
import likelion13th.asahi.onmaeul.repository.CategoryRepository;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static likelion13th.asahi.onmaeul.dto.response.ApiResponse.ok;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final HelpRequestRepository helpRequestRepository;
    private final CategoryRepository categoryRepository;
    private final OpenAiService openAiService;

    @Transactional
    public ApiResponse<DraftResponsePayload> update(DraftRequest updateDto) {
        String sessionId = updateDto.getSessionId();

        //임시 db에서 chatDraft 가져오기
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

    //dto로 변환
    private DraftResponsePayload createFinalDraftResponse(ChatDraft draft) {
        //CollectedDataDto 설정
        CollectedDataDto collectedData = new CollectedDataDto();
        collectedData.setTitle(draft.getTitle());
        collectedData.setDescription(draft.getDescription());
        collectedData.setLocation(draft.getLocation());
        collectedData.setLocationDetail(draft.getLocationDetail());
        collectedData.setRequestTime(draft.getRequestTime());
        collectedData.setImages(draft.getImages());
        collectedData.setSessionId(draft.getSessionId());

        //missingFields, canFinish
        List<String> missingFields = calculateMissingFields(draft);
        boolean canFinish = missingFields.isEmpty();

        //DraftResponsePayload 설정
        DraftResponsePayload response = new DraftResponsePayload();
        response.setCollected(collectedData);
        response.setMissingFields(missingFields);
        response.setCanFinish(canFinish);

        return response;
    }

    //missingFields 검사
    private List<String> calculateMissingFields(ChatDraft draft) {
        List<String> missing = new ArrayList<>();
        if (draft.getTitle() == null || draft.getTitle().isBlank()) missing.add("title");
        if (draft.getLocation() == null || draft.getLocation().isBlank()) missing.add("location");
        if (draft.getLocationDetail() == null || draft.getLocationDetail().isBlank()) missing.add("location_detail");
        if (draft.getRequestTime() == null || draft.getRequestTime().isBlank()) missing.add("request_time");
        return missing;
    }

    @Transactional
    public ApiResponse<FinalChatResponsePayload> createArticle(FinalChatRequest finalChatRequest, User user) {
        String sessionId = finalChatRequest.getSessionId();
        //form 가져오기
        ChatDraft existingDraft = (ChatDraft) redisTemplate.opsForValue().get(sessionId);

        //category 설정
        Long categoryIdByDraft = existingDraft.getCategoryId();
        Category category = categoryRepository.findById(categoryIdByDraft)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 카테고리를 찾을 수 없습니다: " + categoryIdByDraft));

        //HelpRequest domain 만들기
        HelpRequest newHelpRequest = HelpRequest.builder()
                .title(existingDraft.getTitle())
                .description(existingDraft.getDescription())
                .location(existingDraft.getLocation())
                .locationDetail(existingDraft.getLocationDetail())
                .requestTime(OffsetDateTime.parse(existingDraft.getRequestTime()))
                .category(category)
                // .images(existingDraft.getImages())
                .requester(user)
                .build();

        //예상 소요 시간 계산
        Integer estimatedMinutes= estimateMinutes(newHelpRequest);

        newHelpRequest.setEstimatedMinutes(estimatedMinutes);

        //작성 내용을 기반으로 DB에 helpRequest 넣기
        helpRequestRepository.save(newHelpRequest);

        //임시 저장소에서 삭제
        redisTemplate.delete(sessionId);

        FinalChatResponsePayload finalChatResponsePayload = createFinalChatResponsePayload(existingDraft, user);

        return ok("도움 요청이 성공적으로 등록되었습니다.", finalChatResponsePayload);
    }

    //dto 만들기
    public FinalChatResponsePayload createFinalChatResponsePayload(ChatDraft chatDraft, User user) {
        return FinalChatResponsePayload.builder()
                .location(chatDraft.getLocation())
                .requestId(user.getId())
                .title(chatDraft.getTitle())
                .categoryId(chatDraft.getCategoryId())
                .description(chatDraft.getDescription())
                .locationDetail(chatDraft.getLocationDetail())
                .requestTime(chatDraft.getRequestTime())
                //.images()
                .status(HelpRequestStatus.PENDING.toString())
                .createdAt(OffsetDateTime.now().toString())
                .route("/help-requests/" + user.getId()).build();
    }

    //gpt-api를 이용한 예상 소요 시간 구하기
    public Integer estimateMinutes(HelpRequest helpRequest) {
        // 프롬프트
        String prompt = String.format(
                """
                        너는 '온마을'이라는 동네 기반 도움 요청 서비스의 AI 비서야.
                        아래에 있는 도움 요청 내용을 읽고, 이 일을 처리하는 데 몇 분 정도 걸릴지 예상해줘.
                                        
                        - 제목: %s
                        - 내용: %s
                        - 장소: %s
                                        
                        규칙:
                        1. 요청을 이해하고, 실제 도움 제공 시간만 고려해줘.
                        2. 답변은 반드시 '분' 단위의 숫자로만 해줘. (예: 30)
                        3. 다른 어떤 설명이나 단위를 붙이지 말고, 오직 숫자만 응답해야 해.
                        """,
                helpRequest.getTitle(),
                helpRequest.getDescription(),
                helpRequest.getLocation()
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(List.of(new ChatMessage(ChatMessageRole.USER.value(), prompt)))
                .maxTokens(10) // 응답 길이를 짧게 제한
                .temperature(0.2) // 일관된 답변을 위해 온도를 낮게 설정
                .build();

        try {
            // API를 호출하고 응답을 받기
            String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();

            // 응답 Integer로 변환
            return Integer.parseInt(response.trim());

        } catch (Exception e) {
            System.err.println("GPT API 호출 또는 응답 파싱 중 오류 발생: " + e.getMessage());
            return 30; //에러 발생 시 기본값(30분) 반환
        }
    }
}
