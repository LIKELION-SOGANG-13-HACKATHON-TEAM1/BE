package likelion13th.asahi.onmaeul.service;

import jakarta.transaction.Transactional;
import likelion13th.asahi.onmaeul.dto.request.UpdateRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.helpRequest.*;
import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.domain.UserRole;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import likelion13th.asahi.onmaeul.util.CursorUtil;
import likelion13th.asahi.onmaeul.dto.response.helpRequest.HelpRequestArticlePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.List;

import static likelion13th.asahi.onmaeul.dto.response.ApiResponse.ok;


@RequiredArgsConstructor
@Service
public class HelpRequestService {
    private final HelpRequestRepository helpRequestRepository;

    public ApiResponse<HelpRequestPayload> findMain(String nextCursor, HelpRequestStatus status, UserRole role, int limit){

        List<HelpRequest> helpRequests;
        final Pageable pageable= (Pageable) PageRequest.of(0,limit); //cursor를 기반하여 페이지 가져오기에 offset은 항상 0으로 설정

        if(nextCursor==null||nextCursor.isBlank()) {
            //첫페이지 가져오기
            helpRequests = helpRequestRepository.findTop5ByStatusOrderByCreatedAtDescIdDesc(status);

        }else{
            //두번째 이후 페이지 가져오기
            var c = CursorUtil.decode(nextCursor);
            helpRequests = helpRequestRepository.findNextPageByStatus(status, c.createdAt(), c.id(),pageable);
        }

        String newCursor=null;
        boolean hasMore=false;
        if(helpRequests.size()==limit){
            /*5개의 글이 나올 시 다음이 더 있다고 반환
            5개가 안되는 경우 마지막 페이지
             */
            var last = helpRequests.get(helpRequests.size() - 1); //helpRequestItem 속 마지막 helpRequest 가져오기
            newCursor = CursorUtil.encode(last.getCreatedAt(), last.getId());
            hasMore = true;
            //newCursor이 null이 아니고 hasMore이 true여야만 무한 스크롤 제공
        }

        //entity를 dto로 변환
        List<HelpRequestItem> items=helpRequests.stream()
                .map(e->{
                    boolean canAccept = (status == HelpRequestStatus.PENDING)
                            && (role == UserRole.JUNIOR);
                    return HelpRequestItem.builder()
                            .requestId(e.getId())
                            .title(e.getTitle())
                            .location(e.getLocation())
                            .requestTime(e.getRequestTime().toString())
                            .createdAt(e.getCreatedAt().toString())
                            .status(e.getStatus().toString())
                            .route("/help-requests/" + e.getId())
                            .uiFlags(new HelpRequestItem.UiFlags(canAccept))
                            .build();
                })
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

    public ApiResponse<HelpRequestArticlePayload> findArticle(long id, User user) {
        //repository에서 id에 맞는 HelpRequest 가져오기
        HelpRequest helprequest=helpRequestRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("not found: "+id));

        //senior,junior 공통 필드부터 설정
        HelpRequestArticlePayload.HelpRequestArticlePayloadBuilder dtobuilder=HelpRequestArticlePayload.builder()
                .requestId(helprequest.getId())
                .title(helprequest.getTitle())
                .description(helprequest.getDescription())
                .location(helprequest.getLocation())
                .requestTime(helprequest.getRequestTime().toString())
                .images(helprequest.getImages())
                .status(helprequest.getStatus().toString())
                .createdAt(helprequest.getCreatedAt().toString());

        //user가 senior인지 확인
        boolean isSenior=helprequest.getRequester().getId().equals(user.getId());

        if(isSenior){
            //user가 senior
            //routes 필드 설정
            dtobuilder.routes(HelpRequestArticlePayload.Routes.builder()
                    .edit("/help-requests/"+helprequest.getId()+"/edit")
                    .cancel("/help-requests/"+helprequest.getId()+"/cancel")
                    .build());
            //uiFlags 필드 설정
            dtobuilder.uiFlags(HelpRequestArticlePayload.UiFlags.builder()
                    .canAccept(false)
                    .canEdit(helprequest.getStatus()==HelpRequestStatus.PENDING) //PENDING인 경우에만 true
                    .canCancel(helprequest.getStatus()==HelpRequestStatus.PENDING) //PENDING인 경우만 true
                    .build());
        }else{
            //user가 junior
            //writer 필드 설정
            dtobuilder.writer(HelpRequestArticlePayload.Writer.builder()
                    .userId(helprequest.getRequester().getId())
                    .name(helprequest.getRequester().getUsername())
                    .role(helprequest.getRequester().getRole().toString())
                    .build());

            //uiFlags 필드 설정
            dtobuilder.uiFlags(HelpRequestArticlePayload.UiFlags.builder()
                    .canAccept(helprequest.getStatus()==HelpRequestStatus.PENDING) // PENDING 상태일 때만 수락 가능
                    .canEdit(false)
                    .canCancel(false)
                    .build());
        }
        HelpRequestArticlePayload build = dtobuilder.build();
        return ok("요청 상세 조회 성공",build);
    }

    //요청게시글 삭제하기
    // 로그인 기능 개발 후 작성자가 맞는지 확인 코드 작성 필요
    public ApiResponse<DeletePayload> deleteArticle(long id, User user){
        HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        authorizeArticleAuthor(helpRequest);

        helpRequestRepository.delete(helpRequest);

        DeletePayload deletePayload =DeletePayload.builder()
                .requestId(id)
                .status(HelpRequestStatus.CANCELED.toString())
                .deletedAt(OffsetDateTime.now())
                .route("/help-requests")
                .build();

        return ok("요청글이 삭제되었습니다", deletePayload);
    }

    @Transactional
    public ApiResponse<UpdatePayload> patch(long id, User user, UpdateRequest updateRequest){
        HelpRequest helpRequest=helpRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        authorizeArticleAuthor(helpRequest);

        helpRequest.update(updateRequest);

        UpdatePayload updatePayload =UpdatePayload.builder()
                .requestId(id)
                .status(HelpRequestStatus.PENDING.toString())
                .updatedAt(OffsetDateTime.now())
                .route("/help-requests")
                .build();

        return ok("요청글이 수정되었습니다.", updatePayload);
    }

    //게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(HelpRequest helpRequest){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!helpRequest.getRequester().getUsername().equals(userName)){
            throw new IllegalArgumentException("not authorized");
        }
    }
}

