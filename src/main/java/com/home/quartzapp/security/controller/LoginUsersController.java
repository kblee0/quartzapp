package com.home.quartzapp.security.controller;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.security.dto.*;
import com.home.quartzapp.security.service.JwtService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LoginUsersController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @RequestMapping(value = "/v1/users/login", method = RequestMethod.POST)
    public ResponseEntity<?> userLogin(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        Authentication authentication;
        UsernamePasswordAuthenticationToken authenticationToken;

        authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword());

        try {
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (BadCredentialsException e) {
            throw ApiException.code("SCR0001");
        }
        catch (UsernameNotFoundException e) {
            throw ApiException.code("SCR0001");
        }
        catch (Exception e) {
            throw ApiException.code("CMNE0001");
        }

        if(!authentication.isAuthenticated()) throw new UsernameNotFoundException("invalid user request!");

        JwtTokenDto jwtTokenDto = jwtService.generateToken(loginRequestDto.getLoginId());

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(jwtTokenDto.getAccessToken())
                .refreshToken(jwtTokenDto.getRefreshToken())
                .expiresIn(jwtTokenDto.getExpiresIn())
                .tokenType(jwtTokenDto.getTokenType())
                .build();

        return ResponseEntity.ok(loginResponseDto);
        }
}