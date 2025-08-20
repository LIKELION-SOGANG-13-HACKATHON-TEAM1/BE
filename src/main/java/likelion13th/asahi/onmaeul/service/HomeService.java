package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.home.HomeAction;
import likelion13th.asahi.onmaeul.dto.response.home.HomePayload;
import likelion13th.asahi.onmaeul.dto.response.home.JuniorHomePayload;
import likelion13th.asahi.onmaeul.dto.response.home.SeniorHomePayload;
import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.domain.UserRole;
import likelion13th.asahi.onmaeul.repository.HelpRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

import static likelion13th.asahi.onmaeul.dto.response.ApiResponse.ok;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final HelpRequestRepository helpRequestRepository;

    public ApiResponse<? extends HomePayload> getHome(User user) {
        //senior
        if (user.getRole() == UserRole.SENIOR) {
            //도움받기 버튼
            HomeAction action1 = HomeAction.builder()
                    .title("청년에게 도움받기")
                    .subtitle("챗봇이 도움요청을 도와드려요")
                    .route("/request/chat")
                    .action("navigate").build();

        //도움 요청 목록
            HomeAction action2 = HomeAction.builder()
                    .title("도움 요청 목록")
                    .subtitle("이런 도움이 필요해요")
                    .route("/help-requests")
                    .action("navigate").build();

            List<HomeAction> homeActions = List.of(action1, action2);

            SeniorHomePayload payload = SeniorHomePayload.builder()
                    .role("senior")
                    .homeActions(homeActions)
                    .build();


            return ok("어르신용 홈 메인화면 조회 성공",payload);
        }

        //청년용 main 화면
        else{
            String listEndPoint="/helpRequests?status=pending&limit=5";
            JuniorHomePayload payload=JuniorHomePayload.builder()
                    .role("junior")
                    .listEndpoint(listEndPoint)
                    .build();
            return ok("청년용 홈 메인화면 조회 성공",payload);
        }

    }
}
