package com.home.quartzapp.security.service;

import com.home.quartzapp.security.entity.LoginUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
@NoArgsConstructor
public class LoginUserDetails implements UserDetails {
    private String userId;
    private String loginId;
    private String password;
    private String displayName;
    private List<GrantedAuthority> authorities;

    public LoginUserDetails(LoginUser loginUser) {
        this.userId = loginUser.getUserId();
        this.loginId = loginUser.getLoginId();
        this.password = loginUser.getPassword();
        this.displayName = loginUser.getName();
        this.authorities = Arrays.stream(loginUser.getRoles().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    // Spring Security 전용
    @Override
    public String getUsername() {
        return this.loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}