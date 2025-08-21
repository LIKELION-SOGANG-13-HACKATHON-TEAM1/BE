package likelion13th.asahi.onmaeul.dto.request;

import likelion13th.asahi.onmaeul.domain.UserRole;
import lombok.Data;

import java.util.Optional;

@Data
public class AddUserRequest {
    //회원 가입용 dto
    private String username;
    private String phoneNumber;
    private String profileUrl;
    private String introduce; // role이 senior인 경우 null이어야한다
    private String password;
    private UserRole userRole;
    private String birth;
}
