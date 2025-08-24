package likelion13th.asahi.onmaeul.service;
import likelion13th.asahi.onmaeul.config.auth.CustomUserDetails;
import likelion13th.asahi.onmaeul.domain.HelpRequest;

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
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static likelion13th.asahi.onmaeul.dto.response.ApiResponse.ok;


@RequiredArgsConstructor
@Service
public class HelpRequestService {
    private final HelpRequestRepository helpRequestRepository;

    public ApiResponse<HelpRequestPayload> findMain(String nextCursor, HelpRequestStatus status, UserRole role, int limit) {
        // 500 에러 디버깅 및 안정성 강화를 위한 try-catch 블록 추가
        try {
            final int queryLimit = limit + 1;
            final Pageable pageable = PageRequest.of(0, queryLimit);

            List<HelpRequest> helpRequests;

            if (nextCursor == null || nextCursor.isBlank()) {
                helpRequests = helpRequestRepository.findTop5ByStatusOrderByCreatedAtDescIdDesc(status);
            } else {
                    var c = CursorUtil.decode(nextCursor);
                    helpRequests = helpRequestRepository.findNextPageByStatus(status, c.createdAt(), c.id(), pageable);
            }

            boolean hasMore = helpRequests.size() > limit;
            List<HelpRequest> responseItems = hasMore ? helpRequests.subList(0, limit) : helpRequests;
            String newCursor = null;

            if (!responseItems.isEmpty()) {
                var last = responseItems.get(responseItems.size() - 1);
                newCursor = CursorUtil.encode(last.getCreatedAt(), last.getId());
            }

            List<HelpRequestItem> items = responseItems.stream()
                    .map(e->{
                        boolean canAccept = (status == HelpRequestStatus.PENDING)
                                && (role == UserRole.JUNIOR);
                        return HelpRequestItem.builder()
                                .requestId(e.getId())
                                .title(e.getTitle())
                                .location(e.getLocation())
                                .requestTime(e.getRequestTime().toString())
                                .createdAt(e.getCreatedAt().toString())
                                .category(e.getCategory())
                                .route("/help-requests/" + e.getId())
                                .build();
                    })
                    .toList();

            HelpRequestPayload helpRequestPayload = HelpRequestPayload.builder()
                    .items(items)
                    .nextCursor(hasMore ? newCursor : null)
                    .hasMore(hasMore)
                    .build();

            return ok("도움 요청 리스트 조회 성공", helpRequestPayload);

        } catch (Exception e) {
            // 그 외 모든 예상치 못한 예외는 500 에러의 원인을 파악하기 위해 RuntimeException으로 감싸서 던짐
            throw new RuntimeException("메인 리스트 조회 중 예상치 못한 서버 오류가 발생했습니다.", e);
        }
    }

    public ApiResponse<HelpRequestPayload> search(String keyword,UserRole role,int page,int size){
        //pageable 정보
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        //Repository에서 키워드로 데이터 조회
        Page<HelpRequest> helpRequestPage = helpRequestRepository.findByKeywordAndStatus(keyword, HelpRequestStatus.PENDING, pageable);
        List<HelpRequest> helpRequests = helpRequestPage.getContent();

        List<HelpRequestItem>items=helpRequests.stream()
                .map(e->{
                    boolean canAccept=(e.getStatus() == HelpRequestStatus.PENDING)&&(role==UserRole.JUNIOR);//status가 pending이고 role이 junior여만 수락 버튼 뜬다
                    return HelpRequestItem.builder()
                            .requestId(e.getId())
                            .title(e.getTitle())
                            .location(e.getLocation())
                            .requestTime(e.getRequestTime().toString())
                            .createdAt(e.getCreatedAt().toString())
                            .category(e.getCategory())
                            .route("/help-requests/" + e.getId())
                            .build();

                })
                .toList();

        String findRole = role.equals(UserRole.SENIOR) ? "senior" : "junior";

        HelpRequestPayload helpRequestPayload=HelpRequestPayload.builder()
                .items(items)
                .nextCursor(null)//오프셋 기반 페이지네이션 사용했기에 커서 사용은 안한다
                .hasMore(helpRequestPage.hasNext())
                .build();
        return ok("도움 요청 리스트 검색 성공",helpRequestPayload);
    }

    public ApiResponse<HelpRequestArticlePayload> findArticle(long id, CustomUserDetails user) {
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
                .createdAt(helprequest.getCreatedAt().toString())
                .categoryId(helprequest.getCategory().getId())
                .categoryName(helprequest.getCategory().getName())
                .estimatedTimeInMinutes(helprequest.getEstimatedMinutes());

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
    public ApiResponse<DeletePayload> deleteArticle(long id, CustomUserDetails user){
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
    public ApiResponse<UpdatePayload> patch(long id, CustomUserDetails user, UpdateRequest updateRequest){
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

