package com.home.quartzapp.security.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtTokenDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
}
