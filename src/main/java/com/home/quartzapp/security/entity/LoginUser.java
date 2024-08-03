package com.home.quartzapp.security.entity;

import lombok.Data;

@Data
public class LoginUser {
    private int id;
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String roles;
    private String refreshToken;
}
