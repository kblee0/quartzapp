package com.home.quartzapp.security.service;

import com.home.quartzapp.security.entity.LoginUser;
import lombok.Getter;
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
public class LoginUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final String displayName;
    private final List<GrantedAuthority> authorities;

    public LoginUserDetails(LoginUser loginUser) {
        username = loginUser.getLoginId();
        password = loginUser.getPassword();
        displayName = loginUser.getName();
        authorities = Arrays.stream(loginUser.getRoles().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        log.info("isAccountNonExpired is ture");
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        log.info("isAccountNonLocked true");
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        log.info("isCredentialsNonExpired true");
        return true;
    }

    @Override
    public boolean isEnabled() {
        log.info("isEnabled true");
        return true;
    }
}