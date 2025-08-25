package likelion13th.asahi.onmaeul.controller;

import likelion13th.asahi.onmaeul.dto.request.AddUserRequest;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Controller
public class UserApiController {
    private final UserService userService;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> signup(
            @RequestPart("requestDto") AddUserRequest requestDto,
            @RequestPart(value="profile_image", required = false) MultipartFile profileImage) throws IOException {

        Long userId = userService.save(requestDto, profileImage);
        ApiResponse<Long> payload = ApiResponse.ok("회원가IP 완료", userId);

        return ResponseEntity.ok(payload);
    }
}
