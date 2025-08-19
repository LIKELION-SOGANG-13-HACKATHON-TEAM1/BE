package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.response.EditMyPageResponse;
import likelion13th.asahi.onmaeul.dto.response.MyPageResponse;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    public MyPageResponse getMyPageById(Long userId) {
        // DB에서 User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음: id=" + userId));

        // DTO 변환해서 반환
        return MyPageResponse.builder()
                .user_id(user.getId())
                .user_phonenumber(user.getPhoneNumber())
                .user_introduce(user.getIntroduce())
                .user_name(user.getUsername())   // 닉네임 없이 이름만 사용
                .build();
    }

    @Transactional(readOnly = true)
    public EditMyPageResponse getEditPageById(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음: id=" + userId));

        return EditMyPageResponse.builder()
                .user_id(u.getId())
                .user_name(u.getUsername())
                .birth_date(u.getBirthDate() == null ? null : u.getBirthDate().toString()) // 선택 필드
                .user_phonenumber(u.getPhoneNumber())
                .user_introduce(u.getIntroduce())
                .profile_url(u.getProfileUrl())
                .build();
    }

}
