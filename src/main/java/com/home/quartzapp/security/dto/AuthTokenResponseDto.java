package com.home.quartzapp.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthTokenResponseDto {
    String accessToken;
    String refreshToken;
    String tokenType;
    Long expiresIn;
}
