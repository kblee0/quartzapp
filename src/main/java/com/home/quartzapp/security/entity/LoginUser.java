package com.home.quartzapp.security.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {
    private int id;
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String roles;
}
