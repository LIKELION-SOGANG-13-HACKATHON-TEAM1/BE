package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.DTO.response.ApiResponse;
import likelion13th.asahi.onmaeul.DTO.response.helpRequest.HelpRequestItem;
import likelion13th.asahi.onmaeul.DTO.response.helpRequest.HelpRequestPayload;
import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import likelion13th.asahi.onmaeul.domain.UserRole;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import likelion13th.asahi.onmaeul.util.CursorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;

import static likelion13th.asahi.onmaeul.DTO.response.ApiResponse.ok;


@RequiredArgsConstructor
@Service
public class HelpRequestService {
    private final HelpRequestRepository helpRequestRepository;

    public ApiResponse<HelpRequestPayload> findMain(String nextCursor, HelpRequestStatus status, UserRole role, int limit){

        List<HelpRequest> helpRequestItem;
        final Pageable pageable= (Pageable) PageRequest.of(0,limit); //cursor를 기반하여 페이지 가져오기에 offset은 항상 0으로 설정

        if(nextCursor==null||nextCursor.isBlank()) {
            //첫페이지 가져오기
            helpRequestItem = helpRequestRepository.findTop5ByStatusOrderByCreatedAtDescIdDesc(status);

        }else{
            //두번째 이후 페이지 가져오기
            var c = CursorUtil.decode(nextCursor);
            helpRequestItem = helpRequestRepository.findNextPageByStatus(status, c.createdAt(), c.id(),pageable);
        }

        String newCursor=null;
        boolean hasMore=false;
        if(helpRequestItem.size()==limit){
            /*5개의 글이 나올 시 다음이 더 있다고 반환
            5개가 안되는 경우 마지막 페이지
             */
            var last = helpRequestItem.get(helpRequestItem.size() - 1); //helpRequestItem 속 마지막 helpRequest 가져오기
            newCursor = CursorUtil.encode(last.getCreatedAt(), last.getId());
            hasMore = true;
            //newCursor이 null이 아니고 hasMore이 true여야만 무한 스크롤 제공
        }

        //dto 변환
        List<HelpRequestItem> items=helpRequestItem.stream()
                .map(HelpRequestItem::fromEntity)
                .toList();

        String findRole=role.equals(UserRole.SENIOR)?"senior":"junior";

        //HelpRequestPayload build
        HelpRequestPayload helpRequestPayload=HelpRequestPayload.builder()
                .items(items)
                .nextCursor(newCursor)
                .hasMore(hasMore)
                .role(findRole)
                .build();

        return ok(findRole + "도움 요청 리스트 조회 성공",helpRequestPayload);
    }

}
