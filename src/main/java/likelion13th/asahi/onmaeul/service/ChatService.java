package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.dto.ChatDraft;
import likelion13th.asahi.onmaeul.dto.request.DraftRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.CollectedDataDto;
import likelion13th.asahi.onmaeul.dto.response.DraftResponsePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static likelion13th.asahi.onmaeul.dto.response.ApiResponse.ok;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RedisTemplate<String,Object> redisTemplate;

    public ApiResponse<DraftResponsePayload> update(DraftRequest updateDto){
        String sessionId = updateDto.getSessionId();

        //임시 db에서 chatDraft 가져오기
        ChatDraft existingDraft = (ChatDraft) redisTemplate.opsForValue().get(sessionId);

        updateDraftFromDto(existingDraft, updateDto);

        redisTemplate.opsForValue().set(sessionId, existingDraft, 30, TimeUnit.MINUTES);

        DraftResponsePayload draftResponseData = createFinalDraftResponse(existingDraft);

        return ok("초안 업데이트 성공",draftResponseData);

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
        if(draft.getLocationDetail()==null||draft.getLocationDetail().isBlank()) missing.add("location_detail");
        if(draft.getRequestTime()==null||draft.getRequestTime().isBlank()) missing.add("request_time");
        return missing;
    }

}
