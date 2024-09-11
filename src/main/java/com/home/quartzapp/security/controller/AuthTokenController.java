package com.home.quartzapp.security.controller;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.security.dto.AuthTokenRequestDto;
import com.home.quartzapp.security.service.AuthTokenService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
@Tag(name="JWT Token API", description = "JWT token issuance and refresh")
public class AuthTokenController {
    private final AuthTokenService authTokenService;

    @RequestMapping(value = "/auth/token", method = RequestMethod.POST)
    public ResponseEntity<?> authToken(
            @Parameter(schema = @Schema(allowableValues = {"password", "refresh_token"})) @RequestParam(value="grant_type") String grantType,
            @Valid @RequestBody AuthTokenRequestDto authTokenRequestDto) {
        return switch (grantType) {
            case "password" -> ResponseEntity.ok(authTokenService.authTokenByPassword(authTokenRequestDto));
            case "refresh_token" -> ResponseEntity.ok(authTokenService.authTokenByRefreshToken(authTokenRequestDto));
            default -> throw ApiException.code("CMNE0007");
        };
    }
}