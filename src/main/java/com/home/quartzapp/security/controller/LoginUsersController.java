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
        if(grantType.equals("password")) {
            return ResponseEntity.ok(loginUserService.userLogin(loginRequestDto));
        }
        else if(grantType.equals("refresh_token")) {
            return ResponseEntity.ok(loginUserService.refreshLogin(loginRequestDto));
        }
        throw ApiException.code("CMNE0004");
    }
}