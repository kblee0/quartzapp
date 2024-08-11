package com.home.quartzapp.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenRequestDto {
    private String loginId;
    private String password;
    private String refreshToken;
}
