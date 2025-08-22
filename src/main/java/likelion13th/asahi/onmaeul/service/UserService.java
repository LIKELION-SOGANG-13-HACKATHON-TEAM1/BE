package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.UserRole;
import likelion13th.asahi.onmaeul.dto.request.AddUserRequest;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import likelion13th.asahi.onmaeul.domain.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Long save(AddUserRequest dto){
        String introduceToSave=(dto.getUserRole() == UserRole.JUNIOR)?dto.getIntroduce():null;

            return userRepository.save(User.builder()
                    .username(dto.getUsername())
                    .phoneNumber(dto.getPhoneNumber())
                    .profileUrl(dto.getProfileUrl())
                    .passwordHash(bCryptPasswordEncoder.encode(dto.getPassword()))
                    .introduce(introduceToSave)
                    .birthDate(LocalDate.parse(dto.getBirth()))
                    .role(dto.getUserRole()).build()).getId();
    }

}
