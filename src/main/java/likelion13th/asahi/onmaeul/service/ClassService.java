package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.clazz.ClazzPayload;
import likelion13th.asahi.onmaeul.repository.ClassParticipantRepository;
import likelion13th.asahi.onmaeul.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassService {
    private final ClassRepository classRepository;

    public ApiResponse<ClazzPayload> findList(User user){

    }
}
