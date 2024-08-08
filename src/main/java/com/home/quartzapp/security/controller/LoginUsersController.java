package com.home.quartzapp.security.controller;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.security.dto.*;
import com.home.quartzapp.security.service.LoginUserService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
public class LoginUsersController {
    private final LoginUserService loginUserService;

    @RequestMapping(value = "/auth/token", method = RequestMethod.POST)
    public ResponseEntity<?> userLogin(
            @RequestParam(value="grant_type") String grantType,
            @Valid @RequestBody LoginRequestDto loginRequestDto) {
        return switch (grantType) {
            case "password" -> ResponseEntity.ok(loginUserService.userLogin(loginRequestDto));
            case "refresh_token" -> ResponseEntity.ok(loginUserService.refreshLogin(loginRequestDto));
            default -> throw ApiException.code("CMNE0004");
        };
    }
}