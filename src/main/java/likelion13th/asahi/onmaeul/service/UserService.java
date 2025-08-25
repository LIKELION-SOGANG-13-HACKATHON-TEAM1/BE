package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.UserRole;
import likelion13th.asahi.onmaeul.dto.request.AddUserRequest;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import likelion13th.asahi.onmaeul.domain.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final S3Service s3Service;

    public Long save(AddUserRequest dto, MultipartFile profileImage) throws IOException {
        String introduceToSave=(dto.getUserRole() == UserRole.JUNIOR)?dto.getIntroduce():null;
        String profileUrl = s3Service.upload(profileImage);

            return userRepository.save(User.builder()
                    .username(dto.getUsername())
                    .phoneNumber(dto.getPhoneNumber())
                    .profileUrl(profileUrl)
                    .passwordHash(bCryptPasswordEncoder.encode(dto.getPassword()))
                    .introduce(introduceToSave)
                    .birthDate(LocalDate.parse(dto.getBirth()))
                    .role(dto.getUserRole()).build()).getId();
    }

}
