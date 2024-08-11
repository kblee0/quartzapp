package com.home.quartzapp.security.controller;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.security.dto.*;
import com.home.quartzapp.security.service.AuthTokenService;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
public class AuthTokenController {
    private final AuthTokenService authTokenService;

    @RequestMapping(value = "/auth/token", method = RequestMethod.POST)
    public ResponseEntity<?> authToken(
            @RequestParam(value="grant_type") String grantType,
            @Valid @RequestBody AuthTokenRequestDto authTokenRequestDto) {
        return switch (grantType) {
            case "password" -> ResponseEntity.ok(authTokenService.authTokenByPassword(authTokenRequestDto));
            case "refresh_token" -> ResponseEntity.ok(authTokenService.authTokenByRefreshToken(authTokenRequestDto));
            default -> throw ApiException.code("CMNE0004");
        };
    }
}