package likelion13th.asahi.onmaeul.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion13th.asahi.onmaeul.domain.UserRole;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.http.Multipart;

import java.util.Optional;

@Data
public class AddUserRequest {
    //회원 가입용 dto
    private String username;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String introduce; // role이 senior인 경우 null이어야한다
    private String password;
    @JsonProperty("user_role")
    private UserRole userRole;
    private String birth;
}
