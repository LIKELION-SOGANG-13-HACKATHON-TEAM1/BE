package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.dto.request.AddUserRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class UserApiController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(AddUserRequest request){
        ApiResponse<Long> payload=ApiResponse.ok("회원가입 완료",userService.save(request));
        return ResponseEntity.ok(payload);
    }
}
