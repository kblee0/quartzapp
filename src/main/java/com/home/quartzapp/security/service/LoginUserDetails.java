package com.home.quartzapp.security.service;

import com.home.quartzapp.security.entity.LoginUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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