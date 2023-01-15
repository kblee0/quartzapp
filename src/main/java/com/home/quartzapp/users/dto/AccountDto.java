package com.home.quartzapp.users.dto;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Data;

@Data
public class AccountDto {
    private String accountId;
    private String username;
    private String password;
    private String displayName;
    private String status;
    private List<SimpleGrantedAuthority> roles;
}
