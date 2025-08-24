package likelion13th.asahi.onmaeul.config.auth;

import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
스프링 시큐리티가 로그인 시 호출하는 "사용자 로딩" 서비스.
로그인 아이디로 phoneNumber를 사용.
DB(UserRepository)에서 사용자 조회 후 CustomUserDetails로 변환하여 반환.
*/

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        // username 파라미터에는 로그인 아이디가 들어옴. 우리는 phoneNumber를 아이디로 사용!!
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + phoneNumber));

        // 시큐리티 컨텍스트에 들어갈 principal로 변환
        return CustomUserDetails.from(user);
    }
}

