package likelion13th.asahi.onmaeul.config.auth;

import likelion13th.asahi.onmaeul.domain.User;
import likelion13th.asahi.onmaeul.domain.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String phoneNumber; // 로그인 아이디
    private final String password;
    private final UserRole role;

    public CustomUserDetails(Long id, String phoneNumber, String password, UserRole role) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
    }

    public static CustomUserDetails from(User u) {
        return new CustomUserDetails(
                u.getId(),
                u.getPhoneNumber(),
                u.getPasswordHash(),
                u.getRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getUsername() { return phoneNumber; } // phoneNumber를 username으로 매핑!!
    @Override public String getPassword() { return password; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked()  { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

